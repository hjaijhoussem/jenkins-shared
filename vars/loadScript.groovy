def call(String scriptName) {
    // Load script from resources
    def scriptContent = libraryResource "scripts/${scriptName}"
    
    // Write to workspace and make executable
    writeFile file: scriptName, text: scriptContent
    sh "chmod +x ./${scriptName}"
}
