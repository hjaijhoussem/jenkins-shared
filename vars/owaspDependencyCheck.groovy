def call(Map config = [:]) {
    // Default parameter values
    def dirPath = config.dirPath ?: '.'
    def stopBuild = config.stopBuild ?: false
    def nvdApiKey = config.nvdApiKey ?: 'NVD_API_KEY'
    def failOnCVSS = config.failOnCVSS ?: 11
    def scanPath = config.scanPath ?: 'target/**/*.jar'
    def owaspInstallation = config.owaspInstallation ?: 'owasp-12.1.0'
    def outputDir = config.outputDir ?: './'
    def outputFile = config.outputFile ?: 'dependency-check-report.xml'
    def failedTotalCritical = config.failedTotalCritical ?: 1
    def failedTotalHigh = config.failedTotalHigh ?: 4
    def failedTotalLow = config.failedTotalLow ?: 90
    def failedTotalMedium = config.failedTotalMedium ?: 8
    

    echo "Directory Path: ${dirPath}"
    echo "Stop Build: ${stopBuild}"

    withCredentials([string(credentialsId: nvdApiKey, variable: 'NVD_API_KEY')]) {
        dir(path: dirPath) {
            dependencyCheck additionalArguments: """
                --scan $scanPath
                --out $outputDir
                --format HTML --format XML
                --data /var/jenkins_home/dependency-check-data
                --disableKnownExploited
                --disableRubygems
                --disableBundleAudit
                --disableYarnAudit
                --disableAssembly
                --nvdApiKey $NVD_API_KEY
                --failOnCVSS $failOnCVSS
                --prettyPrint
            """, odcInstallation: owaspInstallation
            
            // Fail the build if one of The vulnerability threshold is exceeded
            dependencyCheckPublisher failedTotalCritical: failedTotalCritical,
                                    failedTotalHigh: failedTotalHigh,
                                    failedTotalLow: failedTotalLow,
                                    failedTotalMedium: failedTotalMedium, 
                                    pattern: outputFile, 
                                    stopBuild: stopBuild
        }
    }
}
