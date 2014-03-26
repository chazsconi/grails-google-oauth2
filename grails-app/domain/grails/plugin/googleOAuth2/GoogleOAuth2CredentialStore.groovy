package grails.plugin.googleOAuth2

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.CredentialRefreshListener
import com.google.api.client.auth.oauth2.TokenErrorResponse
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson.JacksonFactory
import com.bloomhealthco.jasypt.GormEncryptedStringType
import grails.util.Holders

class GoogleOAuth2CredentialStore implements CredentialRefreshListener {

	static HttpTransport HTTP_TRANSPORT = new NetHttpTransport()
	static JsonFactory JSON_FACTORY = new JacksonFactory()

	String userRef
	String accessToken
	String refreshToken
	Long expirationTimeMilliseconds

	//Need to build so refreshListener is set
	Credential credential = new GoogleCredential.Builder()
		.setTransport(HTTP_TRANSPORT)
		.setJsonFactory(JSON_FACTORY)
		.setClientSecrets(Holders.config.googleOAuth2.clientId,
		                  Holders.config.googleOAuth2.clientSecret)
		.addRefreshListener(this)
		.build()

	//The credential is set and saved on loading and saving
	static transients=["credential"]

	static constraints = {
		userRef(unique:true)
		accessToken()
		refreshToken()
		expirationTimeMilliseconds()
	}

	static mapping = {
		table 'credential_store'
		accessToken  type: GormEncryptedStringType
		refreshToken type: GormEncryptedStringType
	}

	def beforeValidate() {
		log.info "beforeValidate"
		accessToken = credential.getAccessToken()
		refreshToken = credential.getRefreshToken()
		expirationTimeMilliseconds = credential.getExpirationTimeMilliseconds()
	}

	def afterLoad() {
		log.info "afterLoad"
		credential.setAccessToken(accessToken)
		credential.setRefreshToken(refreshToken)
		credential.setExpirationTimeMilliseconds(expirationTimeMilliseconds)
	}

	void onTokenResponse(Credential credential, TokenResponse tokenResponse) {

		log.info "onTokenResponse() for $userRef"
		//Have to reload as object is problably not in Hibernate session
		def store = GoogleOAuth2CredentialStore.get(id)

		log.info "old credential=${credentialToMap(store.credential)}"
		log.info "new credential=${credentialToMap(credential)}"

		store.credential = credential
		store.save(failOnError:true)
	}

	void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
		log.error "On token error response credential=${credential} tokenErrorResponse=${tokenErrorResponse}"
	}

	/* Outputs the credential in a human friendly format
	 * @return a map of the credential components
	 */
	static credentialToMap(Credential c) {
		if(c==null) return null
		// Obfuscate the tokens
		[credential: 					c,
			accessToken: 					c.getAccessToken() ? "****" + c.getAccessToken()[-6..-1] : null,
			refreshToken: 					c.getRefreshToken() ? "****" + c.getRefreshToken()[-6..-1] : null,
			expiration_time: 				c.getExpirationTimeMilliseconds() ?
			new Date(c.getExpirationTimeMilliseconds()) : null,
			expireInSeconds: 				c.getExpiresInSeconds(),
			refreshListeners:				c.getRefreshListeners()]
	}
}
