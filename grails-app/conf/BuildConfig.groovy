grails.project.work.dir = 'target'

// Needed to override ivy as get a missing dependency
// on org/hamcrest/SelfDescribing
grails.project.dependency.resolver = "maven"
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

        runtime 'org.jasypt:jasypt:1.9.0'
        runtime 'org.jasypt:jasypt-hibernate3:1.9.0'
        runtime 'org.bouncycastle:bcprov-jdk16:1.46'

		compile 'org.hibernate:hibernate-core:3.6.10.Final', {
			excludes 'ant', 'antlr', 'cglib', 'commons-collections', 'commons-logging', 'commons-logging-api',
			         'dom4j', 'h2', 'hibernate-commons-annotations', 'hibernate-jpa-2.0-api', 'hibernate-validator',
			         'javassist', 'jaxb-api', 'jaxb-impl', 'jcl-over-slf4j', 'jboss-jacc-api_JDK4', 'jta',
			         'junit', 'slf4j-api', 'slf4j-log4j12', 'validation-api'
		}

		compile 'org.hibernate:hibernate-commons-annotations:3.2.0.Final', {
			excludes 'commons-logging', 'commons-logging-api', 'jcl-over-slf4j', 'junit', 'slf4j-api', 'slf4j-log4j12'
		}

		runtime 'dom4j:dom4j:1.6.1', {
			excludes 'jaxen', 'jaxme-api', 'junitperf', 'pull-parser', 'relaxngDatatype', 'stax-api',
			         'stax-ri', 'xalan', 'xercesImpl', 'xpp3', 'xsdlib', 'xml-apis'
		}
	}

	plugins {
		build ':release:3.0.1', ':rest-client-builder:2.0.3', {
			export = false
		}

		// This plugin code has been copied into this project as the current version doesn't support Grail 2.4
		// See src directory
		//compile(":jasypt-encryption:1.1.0")
	}
}
