pipeline {
    agent any
    
    parameters {
        // Parameters to specify which branch to deploy for each service
        string(name: 'CUSTOMERS_SERVICE', defaultValue: 'main', description: 'Branch for customers-service')
        string(name: 'VETS_SERVICE', defaultValue: 'main', description: 'Branch for vets-service')
        string(name: 'VISITS_SERVICE', defaultValue: 'main', description: 'Branch for visits-service')
        string(name: 'API_GATEWAY', defaultValue: 'main', description: 'Branch for api-gateway')
        string(name: 'CONFIG_SERVER', defaultValue: 'main', description: 'Branch for config-server')
        string(name: 'DISCOVERY_SERVER', defaultValue: 'main', description: 'Branch for discovery-server')
        string(name: 'ADMIN_SERVER', defaultValue: 'main', description: 'Branch for admin-server')
        string(name: 'GENAI_SERVICE', defaultValue: 'main', description: 'Branch for genai-service')
        
        // Option to clean up developer environment
        booleanParam(name: 'CLEANUP', defaultValue: false, description: 'Clean up developer environment')
    }
    
    environment {
        // Docker Hub credentials
        DOCKERHUB_CREDENTIALS = credentials('duyzhii-dockerhub')
        DOCKER_HUB_USERNAME = 'duyzhii'
        // Base domain for developer testing
        BASE_DOMAIN = 'petclinic.local'
        // Git repository URL
        GIT_REPO_URL = 'https://github.com/Duyzhii/spring-petclinic-microservices.git'
        // Services list
        SERVICES = 'customers-service,vets-service,visits-service,api-gateway,config-server,discovery-server,admin-server,genai-service' 
        DOCKER_BUILDKIT = '1'
    }
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    // Initialize the IMAGE_TAGS map
                    IMAGE_TAGS = [:]
                    
                    if (params.CLEANUP) {
                        echo "🧹 Cleaning up developer environment..."
                        // Add cleanup commands here if needed
                    }
                    
                    echo "🚀 Initializing pipeline for Spring PetClinic Microservices"
                    echo "📦 Services to build: ${SERVICES}"
                }
            }
        }
        
        stage('Checkout') {
            steps {
                script {
                    // Clean workspace
                    deleteDir()
                    
                    // First checkout the main repository for Docker files and project structure
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/main"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            url: "${GIT_REPO_URL}"
                        ]]
                    ])

                    // Now checkout specific branches for each service if needed
                    def serviceList = env.SERVICES.split(',')
                    for (service in serviceList) {
                        def branchParam = service.toUpperCase().replaceAll('-', '_')
                        def branch = params[branchParam] ?: 'main'

                        if (branch != 'main') {
                            echo "📥 Checking out ${service} from branch ${branch}"

                            dir("spring-petclinic-${service}") {
                                checkout([
                                    $class: 'GitSCM',
                                    branches: [[name: "*/${branch}"]],
                                    doGenerateSubmoduleConfigurations: false,
                                    extensions: [],
                                    submoduleCfg: [],
                                    userRemoteConfigs: [[
                                        url: "${GIT_REPO_URL}"
                                    ]]
                                ])
                            }
                        }
                    }

                    echo "🔎 Finished checking out services"
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    // Map để lưu thông tin về các service cần build và commit ID
                    SERVICES_TO_BUILD = [:]
                    
                    // Định nghĩa tên các service và đường dẫn module tương ứng
                    def serviceModuleMap = [:]
                    env.SERVICES.split(',').each { service ->
                        serviceModuleMap[service] = "spring-petclinic-${service}"
                    }
                    
                    // Hàm để lấy commit ID của một module
                    def getCommitId = { module ->
                        dir(module) {
                            return sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                        }
                    }
                    
                    // Hàm kiểm tra xem có cần rebuild image không dựa trên commit ID
                    def shouldRebuildImage = { commitId, service ->
                        def imageExists = sh(script: "docker manifest inspect ${DOCKER_HUB_USERNAME}/${service}:${commitId} >/dev/null 2>&1", returnStatus: true) == 0
                        return !imageExists
                    }
                    
                    // Kiểm tra thay đổi cho từng branch được chỉ định
                    def serviceList = env.SERVICES.split(',')
                    
                    // Kiểm tra thay đổi trong file pom.xml gốc hoặc tài nguyên dùng chung
                    def rootChanges = sh(script: "git diff --name-only HEAD^ | grep -E 'pom.xml|docker/|scripts/|.mvn/|docker-compose.yml' || true", returnStdout: true).trim()
                    boolean sharedResourcesChanged = rootChanges != ''
                    
                    if (sharedResourcesChanged) {
                        echo "⚠️ Phát hiện thay đổi trong tài nguyên dùng chung. Cần build lại tất cả service."
                        
                        for (service in serviceList) {
                            def modulePath = serviceModuleMap[service]
                            def branchParam = service.toUpperCase().replaceAll('-', '_')
                            def branch = params[branchParam] ?: 'main'
                            def commitId = getCommitId(modulePath)
                            
                            SERVICES_TO_BUILD[service] = [
                                'commitId': commitId,
                                'branch': branch,
                                'shouldBuild': shouldRebuildImage(commitId, service)
                            ]
                        }
                    } else {
                        // Kiểm tra từng service có thay đổi không
                        for (service in serviceList) {
                            def modulePath = serviceModuleMap[service]
                            def branchParam = service.toUpperCase().replaceAll('-', '_')
                            def branch = params[branchParam] ?: 'main'
                            
                            // Kiểm tra thay đổi trong service
                            dir(modulePath) {
                                def hasChanges = false
                                if (branch != 'main') {
                                    // Kiểm tra xem branch có thay đổi so với main không
                                    sh "git fetch origin main:refs/remotes/origin/main || true"
                                    def diffOutput = sh(script: "git diff --name-only origin/main...HEAD || echo ''", returnStdout: true).trim()
                                    hasChanges = diffOutput != ''
                                } else {
                                    // Kiểm tra thay đổi trong branch main
                                    def diffOutput = sh(script: "git diff --name-only HEAD^ || echo ''", returnStdout: true).trim()
                                    hasChanges = diffOutput != ''
                                }
                                
                                if (hasChanges) {
                                    def commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                                    SERVICES_TO_BUILD[service] = [
                                        'commitId': commitId,
                                        'branch': branch,
                                        'shouldBuild': shouldRebuildImage(commitId, service)
                                    ]
                                    echo "🔍 Phát hiện thay đổi trong service: ${service} (branch: ${branch})"
                                }
                            }
                        }
                    }
                    
                    // Hiển thị tóm tắt
                    if (SERVICES_TO_BUILD.isEmpty()) {
                        echo "✅ Không phát hiện thay đổi nào. Bỏ qua các bước build và deploy."
                    } else {
                        echo "📋 Danh sách service cần build: ${SERVICES_TO_BUILD.keySet().join(', ')}"
                        SERVICES_TO_BUILD.each { service, info ->
                            echo "  - ${service}: branch=${info.branch}, commit=${info.commitId}, shouldBuild=${info.shouldBuild}"
                        }
                    }
                }
            }
        }

        stage('Build Services') {
            steps {
                script {
                    // First build the parent POM to ensure dependencies are available
                    sh "mvn clean install -N -DskipTests"
                    
                    // Only build services that have changes
                    if (SERVICES_TO_BUILD.isEmpty()) {
                        echo "⏭️ Không có service nào cần build, bỏ qua giai đoạn này"
                        return
                    }
                    
                    // Now build each service with changes
                    SERVICES_TO_BUILD.each { service, info ->
                        def modulePath = "spring-petclinic-${service}"
                        echo "🚀 Building service: ${service} (branch: ${info.branch})"

                        dir(modulePath) {
                            def buildResult = sh(script: "mvn clean package -DskipTests", returnStatus: true)
                            if (buildResult != 0) {
                                error("❌ Maven build failed for ${service}")
                            }

                            // Verify target directory and list its contents for debugging
                            sh "ls -la"
                            sh "ls -la target || echo 'Target directory not found!'"
                            
                            // Store service specific jar path - IMPORTANT: Note the "./" prefix to ensure we're in the correct dir
                            def jarFile = sh(script: "find ./target -name '*.jar' | grep -v original | head -n 1 || echo ''", returnStdout: true).trim()
                            
                            if (jarFile == '' || jarFile.contains("not found")) {
                                error("❌ No .jar file found for ${service} in directory ${pwd()}")
                            } else {
                                echo "✅ JAR built: ${jarFile}"
                                // Store the jar path in the environment for later use
                                SERVICES_TO_BUILD[service]['jarFile'] = jarFile
                            }
                            
                            // Set image tag based on branch and commit
                            def imageTag = info.commitId
                            if (info.branch == 'main') {
                                IMAGE_TAGS[service] = 'latest'
                            } else {
                                IMAGE_TAGS[service] = imageTag
                            }
                            
                            echo "🏷️ Image tag for ${service}: ${IMAGE_TAGS[service]}"
                        }
                    }
                    
                    // Print all image tags for debugging
                    echo "📋 All image tags: ${IMAGE_TAGS}"
                }
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'duyzhii-dockerhub', 
                                                     usernameVariable: 'DOCKER_USERNAME', 
                                                     passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                    }
                    
                    // Only build and push images for services with changes
                    if (SERVICES_TO_BUILD.isEmpty()) {
                        echo "⏭️ Không có service nào cần build Docker image, bỏ qua giai đoạn này"
                        return
                    }
                    
                    def workspaceRoot = pwd()
                    
                    SERVICES_TO_BUILD.each { service, info ->
                        // Skip if we shouldn't build (image already exists with this commit)
                        if (!info.shouldBuild) {
                            echo "⏭️ Đã tồn tại image cho ${service} với commit ${info.commitId}, bỏ qua build"
                            return
                        }
                        
                        def modulePath = "spring-petclinic-${service}"
                        def jarFile = info.jarFile
                        def commitId = info.commitId
                        def branch = info.branch
                        
                        // Determine exposed port based on service
                        def exposedPort
                        switch(service) {
                            case 'customers-service':
                                exposedPort = '8081'
                                break
                            case 'vets-service':
                                exposedPort = '8083'
                                break
                            case 'visits-service':
                                exposedPort = '8082'
                                break
                            case 'api-gateway':
                                exposedPort = '8080'
                                break
                            case 'config-server':
                                exposedPort = '8888'
                                break
                            case 'discovery-server':
                                exposedPort = '8761'
                                break
                            case 'admin-server':
                                exposedPort = '9090'
                                break
                            case 'genai-service':
                                exposedPort = '8084'
                                break
                            default:
                                error("Unknown service: ${service}")
                        }
                        
                        def servicePath = "${workspaceRoot}/${modulePath}"
                        
                        // Remove leading ./ if present in JAR path
                        if (jarFile.startsWith('./')) {
                            jarFile = jarFile.substring(2)
                        }
                        
                        echo "🐳 Building Docker image for ${service} (branch: ${branch}, tag: ${commitId})"
                        echo "Using JAR file: ${jarFile}"
                        
                        // Use a multi-stage Dockerfile approach if possible
                        sh """
                            DOCKER_BUILDKIT=1 docker build \\
                              --build-arg ARTIFACT_NAME=${jarFile} \\
                              --build-arg EXPOSED_PORT=${exposedPort} \\
                              -t ${DOCKER_HUB_USERNAME}/${service}:${commitId} \\
                              --file ${workspaceRoot}/docker/Dockerfile ${servicePath}
                        """

                        echo "📤 Pushing ${service} image to Docker Hub with tag ${commitId}"
                        
                        // Push image to Docker Hub with commit ID tag
                        sh "docker push ${DOCKER_HUB_USERNAME}/${service}:${commitId}"
                    
                        // If this is the main branch, also tag as latest and push
                        if (branch == 'main') {
                            sh "docker tag ${DOCKER_HUB_USERNAME}/${service}:${commitId} ${DOCKER_HUB_USERNAME}/${service}:latest"
                            sh "docker push ${DOCKER_HUB_USERNAME}/${service}:latest"
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                expression { return !params.CLEANUP && !SERVICES_TO_BUILD.isEmpty() }
            }
            steps {
                script {
                    echo "🚀 Deploying to development environment"
                    
                    // Access image tags during deployment
                    echo "⚙️ Using image tags: ${IMAGE_TAGS}"
                    
                    // Example of how to use the stored image tags
                    IMAGE_TAGS.each { service, tag ->
                        echo "Deploying service ${service} with image tag ${tag}"
                        // Add your deployment steps here using the stored tags
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Cleaning up workspace..."
            cleanWs()
        }
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed!"
        }
    }
}