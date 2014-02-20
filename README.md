# Grails Google OAuth2 Plugin

## Introduction

The Grails Google OAuth2 plugin provides integration with Google Accounts to use with services such as Google Calendar.

It handles the OAuth2 control flow including redirecting the user to Google to request authorisation,  storing the credential in the session and DB (encrypted) and obtaining refresh tokens when necessary.

## Use
A list of URLs in your application which require access to the Google Account can be protected by a URL list.  When the user navigates to one of these URLs, the plugin looks for a credential:

 - First in the session as *session.googleCredential*
 - If none is found then it checks in the DB for a credential for the current user
 - If this is not found then it redirects the user to Google to request access.

After granting access the user is redirected back to the your application where the credential is stored in the DB.  Subsequent access should not require further authorisation unless the user explicity revokes your application access to their Google account.

However, you should assume that the protected URLs will have a valid *session.googleCredential* object which can be used by other Google services.

## Configuration
You need to tell the plugin how to find the email address of the current user.  This is used as a key in the DB to store the credential against and also to verify that the user has authorised the matching Google account.  To do this provide a closure in Config.groovy.

If you are storing the user in the session something like this is probably required:

	googleOAuth2.currentUserRef = {->session.user.email}

If you are using Spring Security Service, use this

	googleOAuth2.currentUserRef = {->grails.util.Holders.applicationContext.getBean("springSecurityService").currentUser.email}


You need to obtain a client id from Google before you can grant access.  Once you do this put the details here:

	googleOAuth2.clientId = "<YOUR GOOGLE CLIENT ID>"
	googleOAuth2.clientSecret = "<YOUR SECRET>"


Also, specify which permissions you require the user to authorise, e.g. calendar, user info

	googleOAuth2.scope = 'https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/userinfo.email'

Provide the list of URLs that require access to the Google resources

	googleOAuth2.interceptUrlList = ['/calendar/view', '/calendar/save']

The credential (current access token and refresh token) are encrypted using the [jasypt plugin](http://grails.org/plugin/jasypt-encryption) on the DB.

You therefore need to add the following encryption settings.  Change the password to something strong.  You can also change the other settings if required.

	jasypt {
		algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC"
		providerName = "BC"
		password = "my-password"  // CHANGE THIS TO SOMETHING ELSE!
		keyObtentionIterations = 1000
	}

IMPORTANT! The jasypt plugin requires no unlimited Java encryption to be enabled.  For how to check and enable this please see the jasypt plugin documentation.


## Example

An example controller to get user information.  This assumes the path to the method is protected by the URL map.

```
import groovyx.net.http.RESTClient

class ExampleController {
	def loadUserInfo() {
		def credential = session.googleCredential
		def userInfo = new RESTClient("https://www.googleapis.com/oauth2/v1/")
				.get( path:'userinfo', query: [access_token: credential.accessToken]).data
		render "userInfo = ${userInfo}"
	}
}
```

## Licence

Copyright 2014 Charles Bernasconi

The plugin is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html "Apache License, Version 2.0 - The Apache Software Foundation") b

## Issues

Issues can be raised via the

## Contributing

Pull requests are the preferred method for submitting contributions. Please open an issue via that issue tracker link above and create an issue describing what your contribution addresses.
