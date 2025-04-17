// pipeline {
//     agent any
    
//     parameters {
//         // Parameters to specify which branch to deploy for each service
//         string(name: 'CUSTOMERS_SERVICE', defaultValue: 'main', description: 'Branch for customers-service')
//         string(name: 'VETS_SERVICE', defaultValue: 'main', description: 'Branch for vets-service')
//         string(name: 'VISITS_SERVICE', defaultValue: 'main', description: 'Branch for visits-service')
//         string(name: 'API_GATEWAY', defaultValue: 'main', description: 'Branch for api-gateway')
//         string(name: 'CONFIG_SERVER', defaultValue: 'main', description: 'Branch for config-server')
//         string(name: 'DISCOVERY_SERVER', defaultValue: 'main', description: 'Branch for discovery-server')
//         string(name: 'ADMIN_SERVER', defaultValue: 'main', description: 'Branch for admin-server')
//         string(name: 'GENAI_SERVICE', defaultValue: 'main', description: 'Branch for genai-service')
        
//         // Option to clean up developer environment
//         booleanParam(name: 'CLEANUP', defaultValue: false, description: 'Clean up developer environment')
//     }
    
//     environment {
//         // Docker Hub credentials
//         DOCKERHUB_CREDENTIALS = credentials('duyzhii-dockerhub')
//         DOCKER_HUB_USERNAME = 'duyzhii'
//         // Base domain for developer testing
//         BASE_DOMAIN = 'petclinic.local'
//         // Git repository URL
//         GIT_REPO_URL = 'https://github.com/Duyzhii/spring-petclinic-microservices.git'
//         // Services list
//         SERVICES = 'spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-discovery-server,spring-petclinic-admin-server,spring-petclinic-genai-service' 
//         DOCKER_BUILDKIT = '1'
//     }
    
//     stages {
//         stage('Initialize') {
//             steps {
//                 script {
//                     // Initialize the IMAGE_TAGS map
//                     IMAGE_TAGS = [:]
                    
//                     if (params.CLEANUP) {
//                         echo "🧹 Cleaning up developer environment..."
//                         // Add cleanup commands here if needed
//                     }
                    
//                     echo "🚀 Initializing pipeline for Spring PetClinic Microservices"
//                     echo "📦 Services to build: ${SERVICES}"
//                 }
//             }
//         }
        
//         stage('Checkout') {
//             steps {
//                 script {
//                     // Clean workspace
//                     deleteDir()
                    
//                     // First checkout the main repository for Docker files and project structure
//                     checkout([
//                         $class: 'GitSCM',
//                         branches: [[name: "*/main"]],
//                         doGenerateSubmoduleConfigurations: false,
//                         extensions: [],
//                         submoduleCfg: [],
//                         userRemoteConfigs: [[
//                             url: "${GIT_REPO_URL}"
//                         ]]
//                     ])

//                     // Now checkout specific branches for each service if needed
//                     def serviceList = env.SERVICES.split(',')
//                     for (service in serviceList) {
//                         def shortName = service.trim().replace('spring-petclinic-', '')
//                         def branchParam = shortName.toUpperCase().replaceAll('-', '_')
//                         def branch = params[branchParam] ?: 'main'

//                         if (branch != 'main') {
//                             echo "📥 Checking out ${service} from branch ${branch}"

//                             dir(service.trim()) {
//                                 checkout([
//                                     $class: 'GitSCM',
//                                     branches: [[name: "*/${branch}"]],
//                                     doGenerateSubmoduleConfigurations: false,
//                                     extensions: [],
//                                     submoduleCfg: [],
//                                     userRemoteConfigs: [[
//                                         url: "${GIT_REPO_URL}"
//                                     ]]
//                                 ])
//                             }
//                         }
//                     }

//                     echo "🔎 Finished checking out services"
//                 }
//             }
//         }

//         stage('Build Services') {
//             steps {
//                 script {
//                     def serviceList = env.SERVICES.split(',')
                    
//                     // First build the parent POM to ensure dependencies are available
//                     sh "mvn clean install -N -DskipTests"
                    
//                     // Now build each service
//                     for (service in serviceList) {
//                         def shortName = service.trim().replace('spring-petclinic-', '')
//                         def branchParam = shortName.toUpperCase().replaceAll('-', '_')
//                         def branch = params[branchParam] ?: 'main'

//                         echo "🚀 Building service: ${service} (branch: ${branch})"

//                         dir(service.trim()) {
//                             def commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
//                             echo "🔖 Commit ID: ${commitId}"

//                             def buildResult = sh(script: "mvn clean package -DskipTests", returnStatus: true)
//                             if (buildResult != 0) {
//                                 error("❌ Maven build failed for ${service}")
//                             }

//                             // Verify target directory and list its contents for debugging
//                             sh "ls -la"
//                             sh "ls -la target || echo 'Target directory not found!'"
                            
//                             // Store service specific jar path - IMPORTANT: Note the "./" prefix to ensure we're in the correct dir
//                             def jarFile = sh(script: "find ./target -name '*.jar' | grep -v original | head -n 1 || echo ''", returnStdout: true).trim()
                            
//                             if (jarFile == '' || jarFile.contains("not found")) {
//                                 error("❌ No .jar file found for ${service} in directory ${pwd()}")
//                             } else {
//                                 echo "✅ JAR built: ${jarFile}"
//                                 // Store the jar path in the environment for later use
//                                 env."${shortName.toUpperCase()}_JAR" = jarFile
//                             }
                            
//                             // Store the commit ID for later use
//                             env."${shortName.toUpperCase()}_COMMIT" = commitId
                            
//                             // Set image tag based on branch
//                             if (branch == 'main') {
//                                 IMAGE_TAGS[shortName] = 'latest'
//                             } else {
//                                 IMAGE_TAGS[shortName] = commitId
//                             }
                            
//                             echo "🏷️ Image tag for ${shortName}: ${IMAGE_TAGS[shortName]}"
//                         }
//                     }
                    
//                     // Print all image tags for debugging
//                     echo "📋 All image tags: ${IMAGE_TAGS}"
//                 }
//             }
//         }

//         stage('Build and Push Docker Images') {
//             steps {
//                 script {
//                     withCredentials([usernamePassword(credentialsId: 'duyzhii-dockerhub', 
//                                                      usernameVariable: 'DOCKER_USERNAME', 
//                                                      passwordVariable: 'DOCKER_PASSWORD')]) {
//                         sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
//                     }
//                     def serviceList = env.SERVICES.split(',')

//                     for (service in serviceList) {
//                         def shortName = service.trim().replace('spring-petclinic-', '')
//                         def branchParam = shortName.toUpperCase().replaceAll('-', '_')
//                         def branch = params[branchParam] ?: 'main'
//                         def commitId = env."${shortName.toUpperCase()}_COMMIT"
//                         def jarFile = env."${shortName.toUpperCase()}_JAR" ?: ''
                        
//                         // Remove leading ./ if present in JAR path
//                         if (jarFile.startsWith('./')) {
//                             jarFile = jarFile.substring(2)
//                         }
                        
//                         def imageTag = IMAGE_TAGS[shortName] ?: commitId  // Use stored tag or fallback to commit ID

//                         echo "🐳 Building Docker image for ${shortName} (branch: ${branch}, tag: ${imageTag})"
//                         echo "Using JAR file: ${jarFile}"

//                         // Determine exposed port based on service
//                         def exposedPort
//                         switch(shortName) {
//                             case 'customers-service':
//                                 exposedPort = '8081'
//                                 break
//                             case 'vets-service':
//                                 exposedPort = '8083'
//                                 break
//                             case 'visits-service':
//                                 exposedPort = '8082'
//                                 break
//                             case 'api-gateway':
//                                 exposedPort = '8080'
//                                 break
//                             case 'config-server':
//                                 exposedPort = '8888'
//                                 break
//                             case 'discovery-server':
//                                 exposedPort = '8761'
//                                 break
//                             case 'admin-server':
//                                 exposedPort = '9090'
//                                 break
//                             case 'genai-service':
//                                 exposedPort = '8084'
//                                 break
//                             default:
//                                 error("Unknown service: ${shortName}")
//                         }
                        
//                         def workspaceRoot = pwd()
//                         def servicePath = "${workspaceRoot}/${service.trim()}"
                        
//                         // Make sure we have a valid JAR file
//                         if (jarFile == '') {
//                             error("No JAR file found for ${service}")
//                         }
                        
//                         // Verify Docker file exists
//                         sh "ls -la ${workspaceRoot}/docker/ || echo 'Docker directory not found'"
                        
//                         // Use a multi-stage Dockerfile approach if possible
//                         sh """
//                             DOCKER_BUILDKIT=1 docker build \\
//                               --build-arg ARTIFACT_NAME=${jarFile} \\
//                               --build-arg EXPOSED_PORT=${exposedPort} \\
//                               -t ${DOCKER_HUB_USERNAME}/${shortName}:${commitId} \\
//                               --file ${workspaceRoot}/docker/Dockerfile ${servicePath}
//                         """

//                         echo "📤 Pushing ${shortName} image to Docker Hub with tag ${commitId}"
                        
//                         // Push image to Docker Hub with commit ID tag
//                         sh "docker push ${DOCKER_HUB_USERNAME}/${shortName}:${commitId}"
                    
//                         // If this is the main branch, also tag as latest and push
//                         if (branch == 'main') {
//                             sh "docker tag ${DOCKER_HUB_USERNAME}/${shortName}:${commitId} ${DOCKER_HUB_USERNAME}/${shortName}:latest"
//                             sh "docker push ${DOCKER_HUB_USERNAME}/${shortName}:latest"
//                         }
//                     }
//                 }
//             }
//         }
        
//         stage('Deploy to Development') {
//             when {
//                 expression { return !params.CLEANUP }
//             }
//             steps {
//                 script {
//                     echo "🚀 Deploying to development environment"
                    
//                     // Access image tags during deployment
//                     echo "⚙️ Using image tags: ${IMAGE_TAGS}"
                    
//                     // Example of how to use the stored image tags
//                     IMAGE_TAGS.each { service, tag ->
//                         echo "Deploying service ${service} with image tag ${tag}"
//                         // Add your deployment steps here using the stored tags
//                     }
//                 }
//             }
//         }
        
//         stage('Cleanup') {
//             when {
//                 expression { return params.CLEANUP }
//             }
//             steps {
//                 script {
//                     echo "🧹 Cleaning up development environment"
//                     // Add cleanup steps here using IMAGE_TAGS if needed
//                 }
//             }
//         }
//     }
    
//     post {
//         always {
//             sh 'docker logout'
//             cleanWs()
//         }
//         success {
//             echo "✅ Pipeline completed successfully"
//         }
//         failure {
//             echo "❌ Pipeline failed"
//         }
//     }
// }
pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        SERVICES = "customers-service visits-service vets-service genai-service admin-server config-server api-gateway discovery-server"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "✅ Checked out source code."
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    // Map để lưu thông tin về các service cần build và commit ID
                    SERVICES_TO_BUILD = [:]
                    
                    // Định nghĩa tên các service và đường dẫn module tương ứng
                    def serviceModuleMap = [
                        'customers-service': 'spring-petclinic-customers-service',
                        'visits-service': 'spring-petclinic-visits-service',
                        'vets-service': 'spring-petclinic-vets-service',
                        'genai-service': 'spring-petclinic-genai-service',
                        'admin-server': 'spring-petclinic-admin-server',
                        'config-server': 'spring-petclinic-config-server',
                        'api-gateway': 'spring-petclinic-api-gateway',
                        'discovery-server': 'spring-petclinic-discovery-server'
                    ]
                    
                    // Lấy danh sách các file đã thay đổi
                    def changes = []
                    if (env.CHANGE_TARGET) {
                        // Nếu là PR build
                        sh """
                            git fetch --no-tags origin ${env.CHANGE_TARGET}:refs/remotes/origin/${env.CHANGE_TARGET}
                            git fetch --no-tags origin ${env.GIT_COMMIT}:refs/remotes/origin/PR-${env.CHANGE_ID}
                        """
                        changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET} HEAD", returnStdout: true).trim().split('\n')
                    } else if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT) {
                        // Nếu là branch build với commit thành công trước đó
                        changes = sh(script: "git diff --name-only ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}", returnStdout: true).trim().split('\n')
                    } else {
                        // So sánh với commit trước đó
                        changes = sh(script: "git diff --name-only HEAD^", returnStdout: true).trim().split('\n')
                    }
                    
                    // Kiểm tra thay đổi trong file pom.xml gốc hoặc tài nguyên dùng chung
                    boolean rootPomChanged = changes.any { it == 'pom.xml' }
                    boolean sharedResourcesChanged = changes.any { change ->
                        change.startsWith('docker/') || 
                        change.startsWith('scripts/') || 
                        change.startsWith('.mvn/') ||
                        change == 'docker-compose.yml'
                    }
                    
                    // Nếu tài nguyên dùng chung thay đổi, build tất cả các service
                    if (rootPomChanged || sharedResourcesChanged) {
                        echo "⚠️ Phát hiện thay đổi trong tài nguyên dùng chung. Cần build lại tất cả service."
                        SERVICES.split().each { service ->
                            def commitId = getCommitId(serviceModuleMap[service])
                            SERVICES_TO_BUILD[service] = [
                                'commitId': commitId,
                                'shouldBuild': shouldRebuildImage(commitId, service)
                            ]
                        }
                    } else {
                        // Kiểm tra từng service có thay đổi không
                        SERVICES.split().each { service ->
                            def modulePath = serviceModuleMap[service]
                            if (changes.any { it.startsWith("${modulePath}/") }) {
                                def commitId = getCommitId(modulePath)
                                SERVICES_TO_BUILD[service] = [
                                    'commitId': commitId,
                                    'shouldBuild': shouldRebuildImage(commitId, service)
                                ]
                                echo "🔍 Phát hiện thay đổi trong service: ${service}"
                            }
                        }
                    }
                    
                    // Hiển thị tóm tắt
                    if (SERVICES_TO_BUILD.isEmpty()) {
                        echo "✅ Không phát hiện thay đổi nào. Bỏ qua các bước build và deploy."
                    } else {
                        echo "📋 Danh sách service cần build: ${SERVICES_TO_BUILD.keySet().join(', ')}"
                        SERVICES_TO_BUILD.each { service, info ->
                            echo "  - ${service}: commit=${info.commitId}, shouldBuild=${info.shouldBuild}"
                        }
                    }
                }
            }
        }

        stage('Build and Test') {
            when {
                expression { return !SERVICES_TO_BUILD.isEmpty() }
            }
            steps {
                script {
                    SERVICES_TO_BUILD.each { service, info ->
                        if (info.shouldBuild) {
                            def moduleName = "spring-petclinic-${service}"
                            echo "🔨 Building và testing ${service}..."
                            sh "./mvnw -pl ${moduleName} verify"
                        } else {
                            echo "⏭️ Bỏ qua build cho ${service}, image đã tồn tại."
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        SERVICES_TO_BUILD.each { service, info ->
                            if (info.shouldBuild) {
                                def moduleName = "spring-petclinic-${service}"
                                archiveArtifacts artifacts: "${moduleName}/target/*.jar", fingerprint: true
                            }
                        }
                    }
                }
            }
        }

        stage('Build and Push Docker Images') {
            when {
                expression { return !SERVICES_TO_BUILD.isEmpty() }
            }
            steps {
                script {
                    // Login to Docker Hub once
                    echo "🔐 Đăng nhập vào Docker Hub"
                    sh "echo '${DOCKERHUB_CREDENTIALS_PSW}' | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
                    
                    SERVICES_TO_BUILD.each { service, info ->
                        if (info.shouldBuild) {
                            def moduleName = "spring-petclinic-${service}"
                            def targetImage = "${DOCKERHUB_CREDENTIALS_USR}/${moduleName}:${info.commitId}"
                            
                            echo "🐳 Building Docker image cho ${service}"
                            //sh "docker build -f docker/Dockerfile --build-arg ARTIFACT_NAME=${service}-${version} -t ${DOCKERHUB_CREDENTIALS_USR}/${moduleName}:${info.commitId} ${moduleName}/target"
                            sh "docker build -f docker/Dockerfile --build-arg ARTIFACT_NAME=${service}-latest -t ${DOCKERHUB_CREDENTIALS_USR}/${moduleName}:${info.commitId} ${moduleName}/target"
                            
                            //sh "./mvnw clean install -PbuildDocker -pl ${moduleName}"
                            
                            echo "🏷️ Gắn tag cho image: ${targetImage}"
                            sh "docker tag springcommunity/${moduleName}:latest ${targetImage}"
                            
                            echo "📤 Đẩy image ${targetImage} lên Docker Hub"
                            sh "docker push ${targetImage}"
                        }
                    }
                }
            }
        }

        // stage('Deploy to Kubernetes') {
        //     when {
        //         expression { return !SERVICES_TO_BUILD.isEmpty() && params.DEPLOY_TO_K8S }
        //     }
        //     steps {
        //         script {
        //             echo "🚀 Triển khai các service đã thay đổi lên Kubernetes"
        //             def yaml = SERVICES_TO_BUILD.collect { service, info ->
        //                 def imagePath = "${DOCKERHUB_CREDENTIALS_USR}/spring-petclinic-${service}:${info.commitId}"
        //                 def serviceBlock = (service == 'api-gateway') ? """
        //                   service:
        //                     type: NodePort
        //                     port: 80
        //                     nodePort: 30080
        //                 """ : ""
        //                 """  ${service}:\n    image: ${imagePath}${serviceBlock}"""
        //             }.join("\n")
                    
        //             writeFile file: 'values.yaml', text: "services:\n${yaml}"
        //             sh "helm upgrade --install petclinic ./helm-chart -f values.yaml --namespace developer --create-namespace"
        //         }
        //     }
        // }
    }

    post {
        success {
            echo '✅ CI/CD pipeline hoàn thành thành công!'
        }
        failure {
            echo '❌ CI/CD pipeline thất bại!'
        }
        always {
            echo '🧹 Dọn dẹp workspace...'
            cleanWs()
        }
    }
}

// Lấy commit ID cho một module
def getCommitId(String modulePath) {
    return sh(script: "cd ${modulePath} && git rev-parse --short HEAD", returnStdout: true).trim()
}

// Kiểm tra xem có cần build lại image không
def shouldRebuildImage(String commitId, String service) {
    def imageTag = "${DOCKERHUB_CREDENTIALS_USR}/spring-petclinic-${service}:${commitId}"
    def exists = sh(script: "docker pull ${imageTag} > /dev/null 2>&1 || echo 'missing'", returnStdout: true).trim()
    return (exists == 'missing')
}