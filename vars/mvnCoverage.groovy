def call(Map config = [:]) {
    // Default parameter values
    def dirPath = config.dirPath ?: '.'
    def mavenSettingsId = config.mavenSettingsId ?: 'mvn-nexus-jar'
    
    echo "Directory Path: ${dirPath}"
    echo "Maven Settings ID: ${mavenSettingsId}"

    catchError(buildResult: 'SUCCESS', message: 'Coverage', stageResult: 'UNSTABLE') {
        dir(path: dirPath) {
            configFileProvider([configFile(fileId: mavenSettingsId, variable: 'MAVEN_SETTINGS')]) {
                sh "mvn jacoco:report -s $MAVEN_SETTINGS"
            } 
        }   
    }
}
