def call(String hostAndPort, String junitXmlPath, String coverageXmlPath, String projectName, String buildNumber) {

    def jobName = env.JOB_NAME ?: 'unknown-job'
    def buildStatus = currentBuild.currentResult ?: 'UNKNOWN'
    

    def buildNumberInt
    try {
        buildNumberInt = buildNumber.toInteger()
    } catch (Exception e) {
        echo "ERROR: Failed to convert build number '${buildNumber}' to integer: ${e.message}"
        echo "Using original string value instead."
        buildNumberInt = buildNumber
    }

    // Check if files exist before attempting to send them
    def junitFileExists = fileExists(junitXmlPath)
    def coverageFileExists = fileExists(coverageXmlPath)
    
    echo "Checking pipeline data files:"
    echo "JUnit XML file (${junitXmlPath}) exists: ${junitFileExists}"
    echo "Coverage XML file (${coverageXmlPath}) exists: ${coverageFileExists}"
    
    // Only proceed if both files exist
    if (!junitFileExists || !coverageFileExists) {
        echo "ERROR: One or both required files do not exist. Skipping API call."
        echo "Missing files:"
        if (!junitFileExists) echo "- JUnit XML: ${junitXmlPath}"
        if (!coverageFileExists) echo "- Coverage XML: ${coverageXmlPath}"
        return false
    }
    
    try {
        echo "Sending pipeline data to ${hostAndPort}"
        
        // Construct the full curl command at once
        def curlCmd = """
            curl -v --connect-timeout 30 -X POST \\
                "${hostAndPort}/api/pipeline" \\
                -H "api-version: 1.0.0" \\
                -F "build_nbr=${buildNumberInt}" \\
                -F "project_name=${projectName}" \\
                -F "name=${jobName}" \\
                -F "status=${buildStatus}" \\
                -F "junit_xml=@${junitXmlPath}" \\
                -F "coverage_xml=@${coverageXmlPath}"
        """
        
        def response = sh(script: curlCmd, returnStdout: true).trim()
        echo "API Response: ${response}"
        return true
    } catch (Exception e) {
        echo "ERROR: Failed to send pipeline data: ${e.message}"
        return false
    }
}
