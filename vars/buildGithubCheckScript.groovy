import java.net.HttpURLConnection
import java.net.URL
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.nio.charset.StandardCharsets
import java.io.*
import javax.crypto.KeyGenerator
import java.security.*
import groovy.json.*
import io.jsonwebtoken.*
import java.security.interfaces.RSAPrivateKey
import java.security.spec.*
import static io.jsonwebtoken.SignatureAlgorithm.RS256
import java.util.Base64.Decoder
import org.apache.commons.codec.binary.Base64

// Entry point for the pipeline step
def call() {
    return this
}

// Function to initialize and return global variables
def initializeGlobals(appId, installationId, organizationName) {
    return [
        APP_ID: appId,
        INSTALLATION_ID: installationId,
        ORGANIZATION_NAME: organizationName
    ]
}

// Custom HTTP request method using standard HttpURLConnection
def setRequestMethod(HttpURLConnection connection, String requestMethod) {
    try {
        connection.setRequestMethod(requestMethod)
    } catch (Exception e) {
        throw new RuntimeException("Failed to set request method: ${e.message}", e)
    }
}

// Method to make an HTTP request
def makeHttpRequest(String url, String method, String token, String body = null) {
    try {
        def httpConn = new URL(url).openConnection() as HttpURLConnection
        httpConn.setDoOutput(true)
        httpConn.setRequestMethod(method)
        httpConn.setRequestProperty('Authorization', "token ${token}")
        httpConn.setRequestProperty('Accept', 'application/vnd.github.antiope-preview+json')
        httpConn.setRequestProperty('Content-Type', 'application/json')

        if (body) {
            httpConn.getOutputStream().write(body.getBytes("UTF-8"))
        }

        return httpConn
    } catch (Exception e) {
        throw new RuntimeException("Failed to make HTTP request: ${e.message}", e)
    }
}

// Method to get the previous check run ID
def getPreviousCheckNameRunID(repository, commitID, token, checkName) {
    try {
        def url = "https://api.github.com/repos/${ORGANIZATION_NAME}/${repository}/commits/${commitID}/check-runs"
        def httpConn = makeHttpRequest(url, "GET", token)
        def checkRuns = httpConn.getInputStream().getText()
        def slurperCheckRun = new JsonSlurper()
        def resultMapCheckRun = slurperCheckRun.parseText(checkRuns)
        def checkRun = resultMapCheckRun.check_runs.find { it.name == checkName }
        return checkRun?.id
    } catch (Exception e) {
        error "Failed to retrieve the check id: ${e.message}"
    }
}

// Method to set a check run status
def setCheckName(repository, checkName, status, previousDay, requestMethod, commitID = null, checkRunId = null) {
    try {
        def jsonCheckRun = new JsonBuilder()
        def updateCheckRun = [
            name: checkName,
            status: "in_progress",
            conclusion: status,
            completed_at: previousDay
        ]

        def url = "https://api.github.com/repos/${ORGANIZATION_NAME}/${repository}/check-runs"
        if (requestMethod == "POST") {
            updateCheckRun["head_sha"] = commitID
        } else {
            url += "/${checkRunId}"
        }

        jsonCheckRun(updateCheckRun)
        def httpConn = makeHttpRequest(url, requestMethod, token, jsonCheckRun.toString())
        return httpConn.getResponseCode()
    } catch (Exception e) {
        error "Failed to create a check run: ${e.message}"
    }
}

// Method to get RSA private key
def getRSAPrivateKey(privateKey) {
    try {
        String privateKeyPEM = readFile(privateKey)
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN CERTIFICATE-----\n", "")
        privateKeyPEM = privateKeyPEM.replace("-----END CERTIFICATE-----", "")

        byte[] encoded = Base64.decodeBase64(privateKeyPEM)
        KeyFactory kf = KeyFactory.getInstance("RSA")
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded)
        return (RSAPrivateKey) kf.generatePrivate(keySpec)
    } catch (Exception e) {
        error "Failed to create a RSAPrivateKey: ${e.message}"
    }
}

// Method to generate access time
def accessTime() {
    try {
        Date date = new
