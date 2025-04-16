def call(Map config = [:]) {
    // Default parameter values
    def dirPath = config.dirPath ?: '.'
    def mavenSettingsId = config.mavenSettingsId ?: 'mvn-nexus-jar'
    def maxCacheSize = config.maxCacheSize ?: 1000
    def cachePath = config.cachePath ?: '/root/.m2/repository'
    
    dir(path: dirPath) {
        configFileProvider([configFile(fileId: mavenSettingsId, variable: 'MAVEN_SETTINGS')]) {
            cache(
                maxCacheSize: maxCacheSize,
                caches: [
                    arbitraryFileCache(
                        path: cachePath,
                        cacheValidityDecidingFile: 'pom.xml',
                        includes: '**/*'
                    )
                ]
            ) {
                sh "mvn clean install -DskipTests -s $MAVEN_SETTINGS"
            }
        }
    }
}
