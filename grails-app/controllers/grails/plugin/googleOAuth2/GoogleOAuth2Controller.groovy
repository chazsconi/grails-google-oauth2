package grails.plugin.googleOAuth2

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

class GoogleOAuth2Controller {

	def googleOAuth2Service

	/** Callback path for Google after authorisation.
	 * @return
	 */
	private String getCallbackURL()
	{
		g.createLink(base: grailsApplication.config.grails.serverURL, action: 'callback')
	}

	/** get the reference for the current user (normally an email address)
	 * @return
	 */
	private String getCurrentUserRef() {
		Closure getRef = grailsApplication.config.googleOAuth2.currentUserRef
		getRef()
	}

	/** Asks user to authorise requested access by redirecting to Google
	 * @return
	 */
	def authorize()
	{
		log.info "Requesting authorisation from Google"
		redirect(url: googleOAuth2Service.buildAuthorizeURL(callbackURL, getCurrentUserRef()))
	}

	/** Using passed returned 'code', get and stores Google token in session
	 * @param code
	 * @return
	 */
	def callback(String code)
	{

		try {

			TokenResponse tokenResponse = googleOAuth2Service.exchangeCodeForToken(code, callbackURL)
			GoogleCredential credential = googleOAuth2Service.buildCredentialFromTokenResponse(tokenResponse, getCurrentUserRef())

			log.info "Got new credential=" + GoogleOAuth2CredentialStore.credentialToMap(credential)

			// Get user info and check the user has selected the right google account
			def userInfo = googleOAuth2Service.loadUserInfo(credential)
			if(getCurrentUserRef() != userInfo.email)
			{
				def msg = "You have selected the wrong google account.  You selected ${userInfo.email}, but you should have selected ${getCurrentUserRef()}"
				log.info msg
				flash.message = msg
				redirect url: session.googleAuthFailure
			}
			else
			{
				// Store in session
				if(grailsApplication.config.googleOAuth2.storeCredentialInSession)
					session.googleCredential = credential

				redirect url: session.googleAuthSuccess
			}

		}
		catch (e) {
			log.error e.message, e
			render e
		}
	}

	def checkCredential() {
		def gc = GoogleOAuth2CredentialStore.credentialToMap(session.googleCredential)
		log.info "session.googleCredential=$gc"
		render "session.googleCredential=$gc"
	}

	/** Resets the credential service - just used for testing
	 * @return
	 */
	def resetCredentials() {
		session.removeAttribute('googleCredential')
		String msg = "googleCredential reset in session"
		log.info msg
		render msg
	}

	/** Resets the credential in the DB - just used for testing
	 * @return
	 */
	def resetCredentialsDB() {
		session.removeAttribute('googleCredential')
		googleOAuth2Service.deleteCredential(getCurrentUserRef())
		String msg = "googleCredential reset in session and DB"
		log.info msg
		render msg
	}
}
