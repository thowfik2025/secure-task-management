pipeline {
    agent any

    environment {
        BACKEND_DIR = 'securetask-backend'
        FRONTEND_DIR = 'securetask-frontend'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
    }

    stages {

        stage('Start') {
            steps {
                echo 'Starting Secure Task Management CI/CD Pipeline'
            }
        }

        stage('Check Tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'node --version'
                bat 'npm --version'
            }
        }

        stage('Backend Test') {
            steps {
                dir('securetask-backend') {
                    bat 'mvn test'
                }
            }
        }

        stage('Backend Build') {
            steps {
                dir('securetask-backend') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Frontend Install') {
            steps {
                dir('securetask-frontend') {
                    bat 'npm ci'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('securetask-frontend') {
                    bat 'npm run build'
                }
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'securetask-backend/target/*.jar, securetask-frontend/dist/**',
                                 fingerprint: true
            }
        }
    }

    post {

        success {
            echo 'Backend and Frontend build completed successfully!'
        }

        failure {
            echo 'CI/CD Pipeline failed.'
        }

        always {
            echo 'Pipeline execution completed.'
        }
    }
}