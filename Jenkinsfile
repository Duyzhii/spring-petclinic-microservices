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
        SERVICES = 'spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-discovery-server,spring-petclinic-admin-server,spring-petclinic-genai-service' 
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

                    // Iterate through all services and checkout only the necessary ones
                    def serviceList = env.SERVICES.split(',')
                    for (service in serviceList) {
                        def shortName = service.trim().replace('spring-petclinic-', '')
                        def branchParam = shortName.toUpperCase().replaceAll('-', '_')
                        def branch = params[branchParam] ?: 'main'

                        echo "📥 Checking out ${service} from branch ${branch}"

                        dir(service.trim()) {
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

                    // Also checkout the main project for the Docker files
                    dir('docker') {
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
                    }

                    echo "🔎 Finished checking out services"
                }
            }
        }

        stage('Build Services') {
            steps {
                script {
                    def serviceList = env.SERVICES.split(',')

                    for (service in serviceList) {
                        def shortName = service.trim().replace('spring-petclinic-', '')
                        def branchParam = shortName.toUpperCase().replaceAll('-', '_')
                        def branch = params[branchParam] ?: 'main'

                        echo "🚀 Building service: ${service} (branch: ${branch})"

                        dir(service.trim()) {
                            def commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            echo "🔖 Commit ID: ${commitId}"

                            def buildResult = sh(script: "mvn clean package -DskipTests", returnStatus: true)
                            if (buildResult != 0) {
                                error("❌ Maven build failed for ${service}")
                            }

                            // Store service specific jar path
                            def jarFile = sh(script: "find target -name '*.jar' | grep -v original | head -n 1", returnStdout: true).trim()
                            if (jarFile == '') {
                                error("❌ No .jar file found for ${service}")
                            } else {
                                echo "✅ JAR built: ${jarFile}"
                                // Store the jar path in the environment for later use
                                env."${shortName.toUpperCase()}_JAR" = jarFile
                            }
                            
                            // Store the commit ID for later use
                            env."${shortName.toUpperCase()}_COMMIT" = commitId
                            
                            // Set image tag based on branch
                            if (branch == 'main') {
                                IMAGE_TAGS[shortName] = 'latest'
                            } else {
                                IMAGE_TAGS[shortName] = commitId
                            }
                            
                            echo "🏷️ Image tag for ${shortName}: ${IMAGE_TAGS[shortName]}"
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
                    // Login to Docker Hub
                    withCredentials([string(credentialsId: 'duyzhii-dockerhub', variable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
                        sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKER_HUB_USERNAME --password-stdin'
                    }
                    
                    def serviceList = env.SERVICES.split(',')

                    for (service in serviceList) {
                        def shortName = service.trim().replace('spring-petclinic-', '')
                        def branchParam = shortName.toUpperCase().replaceAll('-', '_')
                        def branch = params[branchParam] ?: 'main'
                        def commitId = env."${shortName.toUpperCase()}_COMMIT"
                        def jarFile = env."${shortName.toUpperCase()}_JAR"
                        def imageTag = IMAGE_TAGS[shortName] ?: commitId  // Use stored tag or fallback to commit ID

                        echo "🐳 Building Docker image for ${shortName} (branch: ${branch}, tag: ${imageTag})"

                        // Determine exposed port based on service
                        def exposedPort
                        switch(shortName) {
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
                                error("Unknown service: ${shortName}")
                        }
                        
                        def workspaceRoot = pwd()
                        def dockerfilePath = "${workspaceRoot}/docker/Dockerfile"
                        def servicePath = "${workspaceRoot}/${service.trim()}"
                        
                        // Full path to the JAR file
                        def fullJarPath = "${servicePath}/${jarFile}"
                        
                        sh """
                            DOCKER_BUILDKIT=1 docker build \\
                              --build-arg ARTIFACT_NAME=${jarFile} \\
                              --build-arg EXPOSED_PORT=${exposedPort} \\
                              -t ${DOCKER_HUB_USERNAME}/${shortName}:${commitId} \\
                              -f ${dockerfilePath} ${servicePath}
                        """

                        echo "📤 Pushing ${shortName} image to Docker Hub with tag ${commitId}"
                        
                        // Push image to Docker Hub with commit ID tag
                        sh "docker push ${DOCKER_HUB_USERNAME}/${shortName}:${commitId}"
                    
                        // If this is the main branch, also tag as latest and push
                        if (branch == 'main') {
                            sh "docker tag ${DOCKER_HUB_USERNAME}/${shortName}:${commitId} ${DOCKER_HUB_USERNAME}/${shortName}:latest"
                            sh "docker push ${DOCKER_HUB_USERNAME}/${shortName}:latest"
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                expression { return !params.CLEANUP }
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
        
        stage('Cleanup') {
            when {
                expression { return params.CLEANUP }
            }
            steps {
                script {
                    echo "🧹 Cleaning up development environment"
                    // Add cleanup steps here using IMAGE_TAGS if needed
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker logout'
            cleanWs()
        }
        success {
            echo "✅ Pipeline completed successfully"
        }
        failure {
            echo "❌ Pipeline failed"
        }
    }
}