def call(String buildStatus = 'STARTED') {
    // Default to SUCCESS if no status provided
    buildStatus = buildStatus ?: 'SUCCESS'

    // Select color based on build status
    def color
    if (buildStatus == 'SUCCESS') {
        color = '#47ec05'  // Green
    } else if (buildStatus == 'UNSTABLE') {
        color = '#d5ee0d'  // Yellow
    } else {
        color = '#ec2805'  // Red
    }
    
    // Send the Slack notification
    slackSend(
        color: color,
        message: "Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
    )
}
