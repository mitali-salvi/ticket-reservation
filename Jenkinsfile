#!groovy

pipeline {
  agent none
  stages {
    stage('Maven Install') {
      agent {
        docker {
          image 'twalter/maven-docker'
        }
      }
      steps {
        sh 'mvn package'
      }
    }

    stage('Docker Build') {
      agent any
      steps {
        sh 'docker build -t mitalisalvi/ticket-reservation-backend:${GIT_COMMIT} .'
      }
    }
    stage('Docker Push') {
      steps {
        container('docker') {
          sh '''
          docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
          docker push mitalisalvi/ticket-reservation-backend:${GIT_COMMIT}
          '''
        }
      }
    }
  }
}