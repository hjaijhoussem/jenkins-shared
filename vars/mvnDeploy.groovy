def call(Map config = [:]) {

    def dirPath = config.dirPath ?: '.'
    def mavenSettingsId = config.mavenSettingsId
    def nexusUrl = config.nexusUrl 
    def deployRepo = config.deployRepo

    echo "Directory Path: ${dirPath}"
    echo "Maven Settings ID: ${mavenSettingsId}"
    echo "Nexus URL: ${nexusUrl}"
    echo "Deploy Repository: ${deployRepo}"

    dir(path: dirPath) {
        configFileProvider([configFile(fileId: mavenSettingsId, variable: 'MAVEN_SETTINGS')]) {
            sh "mvn deploy -DaltDeploymentRepository=${deployRepo}::default::http://${nexusUrl}/repository/hippo-snapshots/ -s $MAVEN_SETTINGS"
        }
    }
}



