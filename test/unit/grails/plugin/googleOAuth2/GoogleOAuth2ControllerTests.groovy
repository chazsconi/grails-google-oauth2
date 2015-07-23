package grails.plugin.googleOAuth2

import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

@TestFor(GoogleOAuth2Controller)
@TestMixin(DomainClassUnitTestMixin)
class GoogleOAuth2ControllerTests {

	private TokenResponse mockTokenResponse = new TokenResponse()
	private GoogleCredential mockCredential = new GoogleCredential()

	void setUp() {
		controller.grailsApplication.config.googleOAuth2.currentUserRef = { -> "angelica@test.com" }
	}

	void testAuthorize() {
		def control = mockFor(GoogleOAuth2Service)
		control.demand.buildAuthorizeURL{ callback, email -> "callback=$callback?email=$email" }
		controller.googleOAuth2Service = control.createMock()

		controller.authorize()

		assert "callback=http://localhost:8080/googleOAuth2/callback?email=angelica@test.com" == response.redirectedUrl
	}

	void doTestValidCallback(storeCredentialInSession) {
		controller.grailsApplication.config.googleOAuth2.storeCredentialInSession = storeCredentialInSession
		def control = mockFor(GoogleOAuth2Service)

		control.demand.exchangeCodeForToken {code, callbackURL ->
			assert "MYCODE" == code
			mockTokenResponse}

		control.demand.buildCredentialFromTokenResponse {tokenResponse, email ->
			assert mockTokenResponse == tokenResponse
			assert "angelica@test.com" == email
			mockCredential}

		control.demand.loadUserInfo {credential ->
			assert mockCredential == credential
			[email:"angelica@test.com"]}

		controller.googleOAuth2Service = control.createMock()

		session.googleAuthSuccess = "SuccessURL"

		controller.callback("MYCODE")

		assert "SuccessURL" == response.redirectedUrl
	}

	void testCallbackStoresCredential() {
		doTestValidCallback(true)
		assert mockCredential == session.googleCredential
	}

	void testCallbackDoesNotStoreCredential() {
		doTestValidCallback(false)
		assert null == session.googleCredential
	}

	void testCallbackWrongUser() {
		def control = mockFor(GoogleOAuth2Service)
		control.demand.exchangeCodeForToken {code, callbackURL -> mockTokenResponse }
		control.demand.buildCredentialFromTokenResponse {tokenResponse, email -> mockCredential}
		control.demand.loadUserInfo {credential -> [email:"fred@test.com"]}
		controller.googleOAuth2Service = control.createMock()
		session.googleAuthFailure = "FailureURL"

		controller.callback("MYCODE")

		assert "FailureURL" == response.redirectedUrl
		assert flash.message =~ /wrong google account/
		assert null == session.googleCredential
	}
}
