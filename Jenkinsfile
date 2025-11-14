pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "magartejas2001"
        BACKEND_IMAGE = "magartejas2001/gov-backend"
        FRONTEND_IMAGE = "magartejas2001/gov-frontend"
        EC2_IP = "18.60.74.20"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/tejasmgr/myprojects.git'
            }
        }

        stage('Build Backend (Spring Boot)') {
            steps {
                dir('govportal/backend') {
                    sh 'mvn clean package -DskipTests'
                    sh "docker build -t ${BACKEND_IMAGE}:latest ."
                }
            }
        }

        stage('Build Frontend (React with Yarn)') {
    steps {
        dir('govportal/frontend') {
            sh 'yarn install'
            sh 'CI=false yarn build'   // <--- IMPORTANT FIX
            sh "docker build -t ${FRONTEND_IMAGE}:latest ."
        }
    }
}
        stage('Push Docker Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'USER',
                                                  passwordVariable: 'PASS')]) {
                    sh "echo $PASS | docker login -u $USER --password-stdin"
                    sh "docker push ${BACKEND_IMAGE}:latest"
                    sh "docker push ${FRONTEND_IMAGE}:latest"
                }
            }
        }

        stage('Deploy on EC2') {
            steps {
                sshagent(['ec2-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_IP} '
                            docker pull ${BACKEND_IMAGE}:latest &&
                            docker pull ${FRONTEND_IMAGE}:latest &&

                            docker stop gov-backend || true &&
                            docker rm gov-backend || true &&
                            docker stop gov-frontend || true &&
                            docker rm gov-frontend || true &&

                            docker run -d -p 8080:8080 --name gov-backend ${BACKEND_IMAGE}:latest &&
                            docker run -d -p 80:80 --name gov-frontend ${FRONTEND_IMAGE}:latest
                        '
                    """
                }
            }
        }
    }
}
