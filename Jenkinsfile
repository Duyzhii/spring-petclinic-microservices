pipeline {
    agent any
    
    environment {
        // Kubernetes config
        KUBECONFIG = credentials('kubeconfig')
    }
    
    stages {
        stage('Cleanup Developer Environment') {
            steps {
                script {
                    // Make sure kubectl uses the correct config
                    sh "mkdir -p \$HOME/.kube"
                    sh "cp ${KUBECONFIG} \$HOME/.kube/config"
                    
                    // Delete all resources in the namespace
                    sh "kubectl delete namespace petclinic-dev --ignore-not-found=true"
                    
                    echo "Developer environment has been cleaned up successfully!"
                }
            }
        }
    }
    
    post {
        always {
            // Clean workspace
            cleanWs()
        }
        
        success {
            echo "Cleanup completed successfully!"
        }
        
        failure {
            echo "Cleanup failed!"
        }
    }
}
