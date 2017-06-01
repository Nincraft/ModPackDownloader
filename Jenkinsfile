node {
   stage 'Checkout'
   git branch: env.BRANCH_NAME, url: 'https://github.com/Nincraft/ModPackDownloader.git'

   stage 'Build'
   bat 'mvnw.cmd clean install'

   stage 'Archive'
   archive excludes: '**/target/Mod*-sources.jar,**/target/Mod*-javadoc.jar', includes: '**/target/Mod*.jar, modpackdownloader-core/target/classes/latest.json'
}