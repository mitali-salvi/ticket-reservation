node("docker") {
    docker.withRegistry('https://hub.docker.com/repository/docker/mitalisalvi/ticket-reservation-backend', 'jenkins-docker') {
    
        git url: "git@github.com:mitali-salvi/ticket-reservation.git", credentialsId: 'backend'
    
        sh "git rev-parse HEAD > .git/commit-id"
        def commit_id = readFile('.git/commit-id').trim()
        println commit_id
    
        stage "build"
        def app = docker.build "mitalisalvi/ticket-reservation-backend"
    
        stage "publish"
        app.push 'master'
        app.push "${commit_id}"
    }
}
