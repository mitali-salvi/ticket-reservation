#!groovy

pipeline {
  agent none
  stages {
    stage('Maven Install') {
      agent {
        docker {
          image 'maven:3.5.0'
        }
      }
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Docker Build') {
      agent any
      steps {
        sh 'docker build -t mitalisalvi/ticket-reservation-backend:latest .'
      }
    }
    stage('Docker Push') {
      agent any
      steps {
        sh '''
        docker login -u mitalisalvi -p Poyhqaz@2410
        docker push mitalisalvi/ticket-reservation-backend:${GIT_COMMIT}
        '''
        }
      }
  }
}