pipeline {
  agent any

  stages {
    stage('Git Clone') {
      steps {
          checkout scm
      }
    }
    
    stage('Build package') {
      steps {
        sh 'cd ${WORKSPACE}' 
        sh 'mvn package'
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