/***
Usage:
  trivyScan.vulnerability(
    imageName: 'my-app:latest',
    severity: 'HIGH',
    exitCode: 1
  )
  trivyScan.reportsConverter()
***/

def vulnerability(Map config) {
    // Load the script
    loadScript('trivy.sh')
    
    // Execute with parameters
    sh "./trivy.sh ${config.imageName} ${config.severity} ${config.exitCode}"
}

def reportsConverter() {
    sh '''
        # Convert LOW,MEDIUM results to JUnit XML and HTML

        trivy convert --format template --template "@/usr/local/share/trivy/templates/junit.tpl" \
        --input trivy-image-MEDUIM-results.json --output trivy-junit-MEDUIM-report.xml
        
        trivy convert --format template --template "@/usr/local/share/trivy/templates/html.tpl" \
        --input trivy-image-MEDUIM-results.json --output trivy-MEDUIM-report.html
        
        # Convert HIGH,CRITICAL results to XML and HTML

        trivy convert --format template --template "@/usr/local/share/trivy/templates/junit.tpl" \
        --input trivy-image-CRITICAL-results.json --output trivy-junit-CRITICAL-report.xml

        trivy convert --format template --template "@/usr/local/share/trivy/templates/html.tpl" \
        --input trivy-image-CRITICAL-results.json --output trivy-CRITICAL-report.html
    '''
}
