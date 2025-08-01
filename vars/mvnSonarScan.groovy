def call(Map config = [:]) {
    // Default parameter values
    def dirPath = config.dirPath ?: '.'
    def mavenSettingsId = config.mavenSettingsId ?: 'mvn-nexus-jar'
    def sonarInstance = config.sonarInstance ?: 'sonar-server'
    def projectKey = config.sonarProjectKey 
    def projectName = config.sonarProjectName
    def reportPath = config.reportPath ?: 'target/site/jacoco/jacoco.xml'
    def sourcesPath = config.sourcesPath ?: 'src/main/java'
    def binariesPath = config.binariesPath ?: 'target/classes'

    echo "Directory Path: ${dirPath}"
    echo "Maven Settings ID: ${mavenSettingsId}"
    echo "Sonar Instance: ${sonarInstance}"
    
    dir(path: dirPath){
        configFileProvider([configFile(fileId: mavenSettingsId, variable: 'MAVEN_SETTINGS')]) {
            withSonarQubeEnv(sonarInstance){
                sh """
                    mvn sonar:sonar \
                        -Dsonar.projectKey=$projectKey \
                        -Dsonar.projectName=$projectName \
                        -Dsonar.coverage.jacoco.xmlReportPaths=$reportPath \
                        -Dsonar.sources=$sourcesPath \
                        -Dsonar.java.binaries=$binariesPath \
                        -s $MAVEN_SETTINGS
                """
            }
        }
    }
}

