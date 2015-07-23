# Grails Google OAuth2 Plugin

## Introduction

The Grails Google OAuth2 plugin provides integration with Google Accounts to use with services such as Google Calendar.

It handles the OAuth2 control flow including redirecting the user to Google to request authorisation,  storing the credential in the session and DB (encrypted) and obtaining refresh tokens when necessary.

## How it works
A list of URLs in your application which require access to the Google Account can be protected by a URL list.  When the user navigates to one of these URLs, the plugin looks for a credential:

 - First in the session as *session.googleCredential*
 - If none is found then it checks in the DB table *credential_store* for a credential for the current user
 - If this is not found then it redirects the user to Google to request access.

After granting access the user is redirected back to the your application where the credential is stored in the DB.  Subsequent access should not require further authorisation unless the user explicity revokes your application access to their Google account.

However, you should assume that the protected URLs will have a valid *session.googleCredential* object which can be used by other Google services.

## Configuration
### Set up account on Google

You need to obtain a Client ID from google.

 1. Go to <https://console.developers.google.com>
 1. In Credentials, create a new Client ID. Specify the callback as http://<yourapp>/googleOAuth2/callback (this can be your localhost for development)
 1. In Consent Screen provide a product name
 1. In APIs select the APIs you want to use.  e.g. Calendar API

### Config.groovy

You need to tell the plugin how to find the email address of the current user.  This is used as a key in the DB to store the credential against and also to verify that the user has authorised the matching Google account.  To do this provide a closure in Config.groovy.

If you are storing the user in the session something like this is probably required:

	googleOAuth2.currentUserRef = {-> session.user.email}

If you are using Spring Security Service, use this

	googleOAuth2.currentUserRef = {-> grails.util.Holders.applicationContext.springSecurityService.currentUser.email}


Enter the Client ID and Secret from Google

	googleOAuth2.clientId = "<YOUR GOOGLE CLIENT ID>"
	googleOAuth2.clientSecret = "<YOUR SECRET>"


Also, specify which permissions you require the user to authorise, e.g. calendar, user info

	googleOAuth2.scope = 'https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/userinfo.email'

Provide the list of URLs that require access to the Google resources, i.e. those where you require the googleCredential to be already in the session.

	googleOAuth2.interceptUrlList = ['/calendar/view', '/calendar/save']

The credentials (current access token and refresh token) are encrypted using the [jasypt plugin](http://grails.org/plugin/jasypt-encryption) on the DB.

You therefore need to add the following encryption settings.  Change the password to something strong.  You can also change the other settings if required.

	jasypt {
		algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC"
		providerName = "BC"
		password = "my-password"  // CHANGE THIS TO SOMETHING ELSE!
		keyObtentionIterations = 1000
	}

IMPORTANT! The jasypt plugin requires no unlimited Java encryption to be enabled.  For how to check and enable this please see the jasypt plugin documentation.

### Serialization problems

If your session needs to be serializable (e.g. you are storing it in a DB, or replicating it) the credential cannot be stored in the session as the class *com.google.api.client.googleapis.auth.oauth2.GoogleCredential* does not implement *java.io.Serializable*.  This will be obvious as you will see *java.io.NotSerializableException* occurring.  It is not easy to override the *GoogleCredential* class and provide a serializable child class as it also has a inner *Builder* class.

To solve this, set the following:

	googleOAuth2.storeCredentialInSession = false

The filter will then not place the credential in the session, however it will ensure it is available in the DB and can be obtained by:

	googleOAuth2Service.loadCredential(session.user.email)

Replace *session.user.email* with the same as you have in *googleOAuth2.currentUserRef*.

This will be less efficient because everytime a protected URL is accessed a DB lookup will occur to verify the credential is in the DB.  An enhancement could be to store a flag in the session to indicate the credential is valid, but this will not deal with the expiry of the session and requesting a refresh token.

## Example Controller

These two example actions assumes the paths are protected by the URL map so that authorisation is first checked.

```
package calendar.example223

import groovyx.net.http.RESTClient
import grails.converters.JSON
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.calendar.Calendar

class CalendarController {

	static HttpTransport HTTP_TRANSPORT = new NetHttpTransport()
	static JsonFactory JSON_FACTORY = new JacksonFactory()

    /** Get user details using the Google REST API.
     * N.B. This won't automatically refresh the access token if necessary
     */
    def userDetails() {
		def userInfo = new RESTClient("https://www.googleapis.com/oauth2/v1/")
				.get( path:'userinfo', query: [access_token: session.googleCredential.accessToken])
				.data
		render userInfo
    }

    /** Get the calendar details fo the user using the Google Java API.
     * This will refresh the access token automatically if necessary
     */
    def calendarEvents() {
		Calendar calendarService =
			new Calendar(HTTP_TRANSPORT, JSON_FACTORY, session.googleCredential)
		def events = calendarService.events()
			.list(grailsApplication.config.googleOAuth2.currentUserRef())
			.execute()
		render events
    }
}
```

## Contributing

Pull requests are the preferred method for submitting contributions.

## Licence

Copyright 2015 Charles Bernasconi

This file is part of the Grails Google OAuth2 Plugin.

The Grails Google OAuth2 Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Grails Google OAuth2 Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Grails Google OAuth2 Plugin.  If not, see <http://www.gnu.org/licenses/>.

