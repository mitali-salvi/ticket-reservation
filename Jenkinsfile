pipeline {
  agent any

  stages {
    stage('Git Clone') {
      steps {
          checkout scm
      }
    }
    
    stage('Build package') {
      agent {
        docker { image 'twalter/maven-docker' }
      }
      steps {
        sh 'cd ${WORKSPACE}' 
        sh 'mvn clean install'
      }
    }
    
    stage('Build image') {
      steps {
        sh '''
        env && docker build -t mitalisalvi/ticket-reservation-backend:${GIT_COMMIT} .
        '''
      }
    }
    
    stage('Push image') {
      steps {
        sh '''
        docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
        docker push mitalisalvi/ticket-reservation-backend:${GIT_COMMIT}
        '''
      }
    }
  }
}