def loginAndBuild(Map config = [:]) {
    // Default parameter values
    def dirPath = config.dirPath ?: '.'
    def credentialsId = config.credentialsId
    def nexusUrl = config.nexusUrl // e.g. 'nexus.example.com'
    def nexusRepository = config.nexusRepository // e.g. 'docker-repo'
    def dockerImageName = config.dockerImageName // e.g. 'my-docker-image'
    def dockerImageVersion = config.dockerImageVersion ?: 'latest'
    
    dir(path: dirPath) {    
        withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh('echo ${PASSWORD} | docker login ${NEXUS_URL} -u ${USERNAME} --password-stdin')
            sh "docker build -t $nexusUrl/repository/$nexusRepository/$dockerImageName:$dockerImageVersion ."
        }
    }
}

def push(Map config = [:]) {
    // Default parameter values
    def nexusUrl = config.nexusUrl // e.g. 'nexus.example.com'
    def nexusRepository = config.nexusRepository // e.g. 'docker-repo'
    def dockerImageName = config.dockerImageName // e.g. 'my-docker-image'
    def dockerImageVersion = config.dockerImageVersion ?: 'latest'
    
    sh "docker push $nexusUrl/repository/$nexusRepository/$dockerImageName:$dockerImageVersion"
}



