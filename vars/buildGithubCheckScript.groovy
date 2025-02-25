@Grab(group='io.jsonwebtoken', module='jjwt', version='0.4')
import sun.net.www.protocol.https.HttpsURLConnectionImpl
import java.text.SimpleDateFormat
import java.lang.reflect.*
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
import org.codehaus.groovy.runtime.GStringImpl

// APP_ID = <APP_ID>
// INSTALLATION_ID = <INSTALLATION_ID>
// ORGANIZATION_NAME = <ORGANIZATION_NAME>

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

// Custom HTTP request method
def setRequestMethod( HttpURLConnection c,  String requestMethod) {
    try {
        final Object target;
        if (c instanceof HttpsURLConnectionImpl) {
            final Field delegate = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
            delegate.setAccessible(true);
            target = delegate.get(c);
        } else {
            target = c;
        }
        final Field f = HttpURLConnection.class.getDeclaredField("method");
        f.setAccessible(true);
        f.set(target, requestMethod);
    } catch (IllegalAccessException | NoSuchFieldException ex) {
        throw new AssertionError(ex);
    }
}

def getPreviousCheckNameRunID(repository, commitID, token, checkName) {
    try {
        def httpConn = new URL("https://api.github.com/repos/${ORGANIZATION_NAME}/${repository}/commits/${commitID}/check-runs").openConnection();
        httpConn.setDoOutput(true)
        httpConn.setRequestProperty( 'Authorization', "token ${token}" )
        httpConn.setRequestProperty( 'Accept', 'application/vnd.github.antiope-preview+json' )
        checkRuns = httpConn.getInputStream().getText();
        def slurperCheckRun = new JsonSlurper()
        def resultMapCheckRun = slurperCheckRun.parseText(checkRuns)
        def check_run_id = resultMapCheckRun.check_runs
                      .find { it.name == checkName }
                      .id
        return check_run_id
    } catch(Exception e){
        error 'Failed to retrieve the check id'
    }           
}

def setCheckName(repository, checkName, status, previousDay, requestMethod, commitID=null, check_run_id=null) {
    try {
        def jsonCheckRun = new groovy.json.JsonBuilder()
        updateCheckRun = ["name":"${checkName}", "status": "in_progress", "conclusion":"${status}", "completed_at": "${previousDay}"]
        def url = "https://api.github.com/repos/${ORGANIZATION_NAME}/${repository}/check-runs"

        if (requestMethod == "POST") {
            updateCheckRun["head_sha"] = "${commitID}"
        } else {
            url += "/${check_run_id}"
        }

        // Cast map to json
        jsonCheckRun updateCheckRun

        def httpConn = new URL(url).openConnection();
        setRequestMethod(httpConn, requestMethod);
        httpConn.setDoOutput(true)
        httpConn.setRequestProperty( 'Authorization', "token ${token}" )
        httpConn.setRequestProperty( 'Accept', 'application/vnd.github.antiope-preview+json' )
        httpConn.getOutputStream().write(jsonCheckRun.toString().getBytes("UTF-8"));
        return httpConn.getResponseCode();
    } catch(Exception e){
        echo "Exception: ${e}"
        error "Failed to create a check run"
    }   
}

def accessTime() {
    try {
        Date date = new Date();
        long t = date.getTime();
        Date expirationTime = new Date(t + 50000l);
        Date iat = new Date(System.currentTimeMillis() + 1000)
        return ["iat": iat, "expirationTime": expirationTime]
    } catch(Exception e){
        echo "Exception: ${e}"
        error "Generated current time failed"
    }    
}

def buildGithubCheck(repository, commitID, accToken, status, checkName) {
    def currentTime = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    def checkName_run_id

    token = accToken
  
    try {
        checkName_run_id = getPreviousCheckNameRunID(repository, commitID, token, checkName)
    } catch(Exception e) {
        echo "Exception: ${e}"
        echo "Check name does not exist"
    }

    if (checkName_run_id) {
        getStatusCode = setCheckName(repository, checkName, status, currentTime, "PATCH", commitID, checkName_run_id)
    } else {
        getStatusCode = setCheckName(repository, checkName, status, previousDay, "POST", commitID)
    }
    if (!(getStatusCode in [200,201])) {
        error "Failed to create a check run, status code: ${getStatusCode}"
    }
}
