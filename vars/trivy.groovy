def scan(Map config = [:]) {

    def nexusUrl = config.nexusUrl // e.g. 'nexus.example.com
    def nexusRepository = config.nexusRepository // e.g. 'docker-repo'
    def dockerImageName = config.dockerImageName // e.g. 'my-docker-image'
    def dockerImageVersion = config.dockerImageVersion

    echo "Nexus URL: ${nexusUrl}"
    echo "Nexus Repository: ${nexusRepository}"
    echo "Docker Image tag: ${dockerImageName}:${dockerImageVersion}"
    
    def trivyHome = tool name: 'trivy-0.58.1', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'

    sh 'curl -o html.tpl https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl'

    sh """
        ${trivyHome}/trivy image --format template --template "@html.tpl" \\
            -o trivy-image-MEDIUM-results.html \\
            --severity LOW,MEDIUM,HIGH \\
            --exit-code 0 \\
            --quiet \\
            ${nexusUrl}/repository/${nexusRepository}/${dockerImageName}:${dockerImageVersion}
    """

    sh """
        ${trivyHome}/trivy image --format template --template "@html.tpl" \\
            -o trivy-image-CRITICAL-results.html \\
            --severity CRITICAL \\
            --exit-code 1 \\
            --quiet \\
            ${nexusUrl}/repository/${nexusRepository}/${dockerImageName}:${dockerImageVersion}
    """
}