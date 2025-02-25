def call() {
    return this
}

def initializeGlobals(appId, installationId, organizationName) {
    return [
        APP_ID: appId,
        INSTALLATION_ID: installationId,
        ORGANIZATION_NAME: organizationName
    ]
}

def getPreviousCheckNameRunID(repository, commitID, token, checkName, globals) {
    echo "executing getPreviousCheckNameRunID function"
    echo "ORGANIZATION_NAME: ${globals.ORGANIZATION_NAME}, INSTALLATION_ID: ${globals.INSTALLATION_ID}, APP_ID: ${globals.APP_ID}"
    try {
        def url = "https://api.github.com/repos/${globals.ORGANIZATION_NAME}/${repository}/commits/${commitID}/check-runs"
        def httpConn = new URL(url).openConnection() as HttpURLConnection
        httpConn.requestMethod = "GET"
        httpConn.setRequestProperty("Authorization", "token ${token}")
        httpConn.setRequestProperty("Accept", "application/vnd.github+json")
        httpConn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        
        def responseText = httpConn.inputStream.text
        def slurper = new JsonSlurper()
        def resultMap = slurper.parseText(responseText)
        def checkRunId = resultMap.check_runs.find { it.name == checkName }?.id
        httpConn.disconnect()
        echo "getPreviousCheckNameRunID completed successfully, checkRunId = ${checkRunId}"
        return checkRunId
    } catch (Exception e) {
        error "Failed to retrieve the check ID: ${e.message}"
    }
}

def setCheckName(repository, checkName, status, previousDay, requestMethod, accToken, commitID = null, check_run_id = null, globals) {
    echo "executing setCheckName function"
    try {
        def jsonBuilder = new JsonBuilder()
        def updateCheckRun = [
            name: checkName,
            status: status,
            started_at: previousDay,
            output: [
                title: checkName,
                summary: "",
                text: ""
            ]
        ]

        if (status == "completed") {
            updateCheckRun["conclusion"] = "success" // or "failure" based on your logic
            updateCheckRun["completed_at"] = previousDay
        }

        def url = "https://api.github.com/repos/${globals.ORGANIZATION_NAME}/${repository}/check-runs"
        if (check_run_id) {
            url += "/${check_run_id}"
        }

        if (requestMethod == "POST") {
            updateCheckRun["head_sha"] = commitID
            updateCheckRun["external_id"] = "42" // Replace with a unique identifier if needed
        }

        jsonBuilder(updateCheckRun)
        def payload = jsonBuilder.toString()

        def httpConn = new URL(url).openConnection() as HttpURLConnection
        httpConn.requestMethod = requestMethod
        httpConn.doOutput = true
        httpConn.setRequestProperty("Authorization", "token ${accToken}")
        httpConn.setRequestProperty("Accept", "application/vnd.github+json")
        httpConn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        httpConn.setRequestProperty("Content-Type", "application/json")

        httpConn.outputStream.withWriter("UTF-8") { it.write(payload) }
        def responseCode = httpConn.responseCode
        def responseText = httpConn.inputStream.text
        httpConn.disconnect()

        echo "setCheckName completed successfully, responseCode = ${responseCode}, responseText = ${responseText}"
        return responseCode
    } catch (Exception e) {
        echo "Exception: ${e.message}"
        error "Failed to create/update check run"
    }
}

def accessTime() {
    echo "executing accessTime function"
    try {
        def now = new Date()
        def expirationTime = new Date(now.time + 50000)
        def iat = new Date(System.currentTimeMillis() + 1000)
        echo "accessTime completed successfully"
        return [iat: iat, expirationTime: expirationTime]
    } catch (Exception e) {
        echo "Exception: ${e.message}"
        error "Failed to generate current time"
    }
}

def buildGithubCheck(repository, commitID, accToken, status, checkName, globals) {
    def currentTime = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    def checkNameRunId
    echo "executing buildGithubCheck function"
    try {
        echo "currentTime: ${accToken}, token: ${accToken}, repository: ${repository}, commitID: ${commitID}, status: ${status}, checkName: ${checkName}"
        checkNameRunId = getPreviousCheckNameRunID(repository, commitID, accToken, checkName, globals)
        echo "buildGithubCheck try catch completed successfully"
    } catch (Exception e) {
        echo "token: ${accToken}, repository: ${repository}, commitID: ${commitID}, status: ${status}, checkName: ${checkName}"
        echo "Exception: ${e.message}"
        echo "Check name does not exist"
    }

    def getStatusCode
    if (checkNameRunId) {
        getStatusCode = setCheckName(repository, checkName, status, currentTime, "PATCH", accToken, commitID, checkNameRunId, globals)
        echo "getStatusCode: ${getStatusCode}"
    } else {
        echo "getStatusCode: ${getStatusCode}"
        getStatusCode = setCheckName(repository, checkName, status, currentTime, "POST", accToken, commitID, globals)
    }

    if (!(getStatusCode in [200, 201])) {
        error "Failed to create/update check run, status code: ${getStatusCode}"
    }
}
