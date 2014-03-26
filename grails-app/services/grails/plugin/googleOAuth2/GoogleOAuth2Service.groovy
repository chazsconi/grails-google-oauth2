package grails.plugin.googleOAuth2

import static groovyx.net.http.ContentType.URLENC
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient

import org.springframework.transaction.annotation.Transactional

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

// The service is difficult to test as it would involve mocking a lot of REST APIs
// also there is currently no logic in the service.
class GoogleOAuth2Service {

	def grailsApplication

	/** Builds the Google authorisation URL to authorise the access
	 * @param callbackURL
	 * @return
	 */
	String buildAuthorizeURL(String callbackURL, String loginHintEmail) {
		def p	= [	scope: 			grailsApplication.config.googleOAuth2.scope,
			redirect_uri: 	callbackURL,
			response_type:	'code',
			client_id:		grailsApplication.config.googleOAuth2.clientId,
			access_type:	'offline',
			approval_prompt:'force', //Forces request for refresh token
			login_hint:		loginHintEmail]

		"https://accounts.google.com/o/oauth2/auth?" + p.collect { it }.join('&')
	}

	/** Exchanges a OAuth2 code for a response object via REST call
	 * @param code
	 * @param callbackURL the URL on this application to which the code was passed
	 * @return OAuth2 response object
	 * @throws HttpResponseException if 200 is not returned from REST call
	 */
	TokenResponse exchangeCodeForToken(String code, String callbackURL) throws HttpResponseException {
		log.info "Exchanging code for token"
		def google = new RESTClient("https://accounts.google.com/o/oauth2/")

		def body = [code: 			code,
			client_id: 		grailsApplication.config.googleOAuth2.clientId,
			client_secret: 	grailsApplication.config.googleOAuth2.clientSecret,
			redirect_uri: 	callbackURL,
			grant_type: 	'authorization_code']

		def response = google.post( path:'token', body: body, requestContentType: URLENC)
		def json = response.data

		new TokenResponse( 	accessToken: 		json.access_token,
		tokenType: 			json.token_type,
		expiresInSeconds: 	120,//json.expires_in,
		refreshToken: 		json.refresh_token,
		scope: 				json.scope)
	}

	/** Builds a GoogleCredential from a tokenResponse object
	 * @param tokenResponse
	 * @param storeEmail email address to store the credential against in the DB
	 * @return
	 */
	@Transactional
	GoogleCredential buildCredentialFromTokenResponse(TokenResponse tokenResponse, String userRef) {

		log.info "Building credential for $userRef"
		GoogleOAuth2CredentialStore store = GoogleOAuth2CredentialStore.findByUserRef(userRef) ?:
				new GoogleOAuth2CredentialStore(userRef: userRef)

		store.credential.setFromTokenResponse(tokenResponse)
		store.save(failOnError:true)
		store.credential
	}

	/** Loads a credential for the given userRef
	 * @param userRef
	 * @return credential null if not found
	 */
	GoogleCredential loadCredential(String userRef) {
		log.info "Loading credential for $userRef"
		GoogleOAuth2CredentialStore store = GoogleOAuth2CredentialStore.findByUserRef(userRef)
		store?.credential
	}

	/** Deletes the credential in the database
	 * @param userRef
	 */
	@Transactional
	void deleteCredential(String userRef) {
		log.info "Deleting credential for $userRef"
		GoogleOAuth2CredentialStore store = GoogleOAuth2CredentialStore.findByUserRef(userRef)
		store.delete(flush:true)
	}

	/** loads user info using the google REST API
	 * @param credential
	 * @return
	 */
	private loadUserInfo(GoogleCredential credential)
	{
		new RESTClient("https://www.googleapis.com/oauth2/v1/")
				.get( path:'userinfo', query: [access_token: credential.accessToken]).data
	}
}
