def call(Map config = [:]) {
    // Default parameter values
    def dirPath = config.dirPath ?: '.'
    def mavenSettingsId = config.mavenSettingsId ?: 'mvn-nexus-jar'
    def skipSurefire = config.skipSurefire ?: false
    
    dir(path: dirPath) {
        configFileProvider([configFile(fileId: mavenSettingsId, variable: 'MAVEN_SETTINGS')]) {
            // Run tests
            sh "mvn test -s $MAVEN_SETTINGS"
            
            // Generate surefire report (unless skipped)
            if (!skipSurefire) {
                sh "mvn surefire-report:report -s $MAVEN_SETTINGS"
            }
        }
    }
}