pipeline {
  agent none
   stages {     
    stage('Maven Install') {
      agent {         
       docker {          
         image 'maven:3.5.0'         
     }       
  }  

  stages {
    stage('Git Clone') {
      steps {
          checkout scm
      }
    }
    
    stage('Build package') {
      steps {
        container('docker') {
          sh 'cd ${WORKSPACE}' 
          sh 'mvn clean install'
        }
      }
    }
    
    stage('Build image') {
      steps {
        container('docker') {
              
          sh '''
          env && docker build -t mitalisalvi/ticket-reservation-backend:${GIT_COMMIT} .
          '''
        }
      }
    }
    
    stage('Push image') {
      steps {
        container('docker') {
          sh '''
          docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
          docker push mitalisalvi/ticket-reservation-backend:${GIT_COMMIT}
          '''
        }
      }
    }

    stage('List pods') {
    steps {
      container('kubectl') {
      sh '''
      kubectl -n api set image deployment/backend mitalisalvi/ticket-reservation-backend:${GIT_COMMIT} --record
      '''
        }
    }
  }
  }
}