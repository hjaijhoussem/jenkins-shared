def call(){
  cache(maxCacheSize: 550, caches: [
      arbitraryFileCache(
          cacheName: 'npm-dependency-cache',
          cacheValidityDecidingFile: 'package-lock.json',
          includes: '**/*',
          path: 'node_modules'
      )
  ]) {
      sh 'npm install --force --no-audit'
  }
}
