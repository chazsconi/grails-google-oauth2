grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        mavenCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
		mavenRepo "http://google-api-client-libraries.appspot.com/mavenrepo"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.21'
        compile 'com.google.http-client:google-http-client-jackson:1.17.0-rc'
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.6'
		compile 'com.google.apis:google-api-services-oauth2:v2-rev64-1.17.0-rc'
    }

    plugins {
        runtime(":hibernate:$grailsVersion")
        build(":tomcat:$grailsVersion",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            	export = false
		    	}
		compile(":jasypt-encryption:1.1.0")
    }
}
