pipeline {
  agent docker {
    image 'openjdk:8-jdk-alpine'
  }

  stages {
    stage('Git Clone') {
      steps {
          checkout scm
      }
    }
    
    stage('Build package') {
      agent  {
        docker { image 'twalter/maven-docker' }
      }
      steps {
        sh 'cd ${WORKSPACE}' 
        sh 'mvn package'
      }
    }
    
    stage('Build image') {
      steps {
        sh '''
        docker build -t mitalisalvi/ticket-reservation-backend:${GIT_COMMIT} .
        '''
      }
    }
    
    stage('Push image') {
      steps {
        sh '''
        docker login -u mitalisalvi -p Poyhqaz@2410
        docker push mitalisalvi/ticket-reservation-backend:${GIT_COMMIT}
        '''
      }
    }
  }
}