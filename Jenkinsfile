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
        SERVICES = 'spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-api-gateway,spring-petclinic-config-server, spring-petclinic-discovery-server, spring-petclinic-admin-server' 
        DOCKER_BUILDKIT = '1'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    // Initialize the IMAGE_TAGS map
                    IMAGE_TAGS = [:]
                    
                    if (params.CLEANUP) {
                        echo "Cleaning up developer environment..."
                        // Add cleanup commands here if needed
                    }
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

                sh 'ls -la'

                echo "🚀 Building service: ${service} (branch: ${branch})"

                dir('.') {
                    def buildResult = sh(
                        script: "./mvnw -pl ${service.trim()} -am clean package -DskipTests",
                        returnStatus: true
                    )

                    if (buildResult != 0) {
                        error("❌ Maven build failed for ${service}")
                    }

                    def jarFile = sh(
                        script: "find ${service.trim()}/target -name '*.jar' | head -n 1",
                        returnStdout: true
                    ).trim()

                    if (jarFile == '') {
                        error("❌ No .jar file found for ${service}")
                    } else {
                        echo "✅ JAR built: ${jarFile}"
                    }
                }
            }
        }
    }
}

    stage('Build and Push Docker Images') {
    steps {
        script {
            // Login to Docker Hub
            sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKER_HUB_USERNAME --password-stdin'
            
            def serviceList = env.SERVICES.split(',')

            for (service in serviceList) {
                def shortName = service.trim().replace('spring-petclinic-', '')
                def branchParam = shortName.toUpperCase().replaceAll('-', '_')
                def branch = params[branchParam] ?: 'main'

                echo "🐳 Building Docker image for ${service} (branch: ${branch})"

                dir(service.trim()) {
                            // Get the latest commit ID for the branch
                            def commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            
                            echo "Building ${service} from branch ${branch} with commit ID ${commitId}"
                            
                            // Store the image tag to be used for deployment
                            if (branch == 'main') {
                                IMAGE_TAGS[service] = 'latest'
                            } else {
                                IMAGE_TAGS[service] = commitId
                            }
                             def jarFile = sh(
                                script: "find ${serviceDir}/target -name '*.jar' | grep -v original | head -n 1",
                                returnStdout: true
                            ).trim()
                            
                            if (jarFile == '') {
                                error("❌ No .jar file found for ${service}")
                            }
                
                            
                            // Determine artifact name based on service
                            def artifactName = "spring-petclinic-${service}"
                            
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
                                default:
                                    error("Unknown service: ${service}")
                            }
                           def DOCKER_BUILDKIT = '1'
                           def dockerfilePath = "${env.WORKSPACE}/docker/Dockerfile"
                            def workspaceRoot = pwd()
                
                            sh """
                                DOCKER_BUILDKIT=1 docker build \\
                                  --build-arg ARTIFACT_NAME=${jarFile} \\
                                  --build-arg EXPOSED_PORT=${exposedPort} \\
                                  -t ${DOCKER_HUB_USERNAME}/${service}:${commitId} \\
                                  -f ${workspaceRoot}/docker/Dockerfile ${workspaceRoot}
                            """

                            echo "Pushing ${service} image to Docker Hub with tag ${commitId}"
                            
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
        }
    }
    
    post {
        always {
            echo "Pipeline execution complete"
            sh "docker logout || true"
        }
        success {
            echo "Pipeline executed successfully"
        }
        failure {
            echo "Pipeline execution failed"
        }
    }
}