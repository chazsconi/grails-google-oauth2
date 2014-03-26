grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://google-api-client-libraries.appspot.com/mavenrepo"
	}

	dependencies {
		compile 'com.google.http-client:google-http-client-jackson:1.17.0-rc'
		compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.6'
		compile 'com.google.apis:google-api-services-oauth2:v2-rev64-1.17.0-rc'
	}

	plugins {
		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}

		runtime(":hibernate:$grailsVersion")
		compile(":jasypt-encryption:1.1.0")
	}
}
