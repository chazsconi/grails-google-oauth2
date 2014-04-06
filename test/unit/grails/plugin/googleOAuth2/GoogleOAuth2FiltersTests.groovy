package grails.plugin.googleOAuth2

import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(GoogleOAuth2Controller)
@Mock(GoogleOAuth2Filters)
class GoogleOAuth2FiltersTests {

	def googleOAuth2ServiceControl

	void mockConfig() {
		controller.grailsApplication.config.googleOAuth2.currentUserRef = {->"test@test.com"}
		controller.grailsApplication.config.googleOAuth2.interceptUrlList = ['/admin/action1',
		                                                                     '/admin/action2']
	}

	void mockServices() {
		// Due to the strange way in testing filters, this convoluted
		// method of mocking is required for services used in the filter
		defineBeans {
			googleOAuth2ServiceControl(GrailsMock, GoogleOAuth2Service)
			googleOAuth2Service(googleOAuth2ServiceControl:"createMock")
		}
		googleOAuth2ServiceControl = applicationContext.getBean("googleOAuth2ServiceControl")
	}

	void testUnfilteredAction() {
		mockConfig()
		request.forwardURI = '/admin/unfiltered'

		controller.metaClass.unfiltered {-> render "OK"}

		withFilters(action:'unfiltered') {
			controller.unfiltered()
		}
		assert null == response.redirectedUrl
		assert 200 == response.status
	}

	void testCredentialInSession() {
		mockConfig()
		request.forwardURI = '/admin/action1'
		session.googleCredential = new Object()

		controller.metaClass.filtered {-> render "OK"}

		withFilters(action:'action1') {
			controller.filtered()
		}
		assert null == response.redirectedUrl
		assert 200 == response.status
	}

	void doTestActionFiltered(String action) {
		mockConfig()
		mockServices()

		request.addHeader('referer','/refering/page')
		request.forwardURI = "/admin/$action"

		googleOAuth2ServiceControl.demand.loadCredential { String ref -> null }

		withFilters(action: action) {
			controller.edit()
		}
		assert "/googleOAuth2/authorize" == response.redirectedUrl
		assert "/admin/$action" == session.googleAuthSuccess
		assert "/refering/page" == session.googleAuthFailure
		assert 302 == response.status
	}

	void testActionFilteredAction1() {
		doTestActionFiltered("action1")
	}

	void testActionFilteredAction2() {
		doTestActionFiltered("action2")
	}

	void testCredentialInDB() {
		mockConfig()
		mockServices()

		request.forwardURI = '/admin/action1'
		def dummyCredential = new Object()

		googleOAuth2ServiceControl.demand.loadCredential { String ref ->
			assert "test@test.com" == ref
			dummyCredential }

		controller.metaClass.edit {-> render "OK"}

		withFilters(action:'action1') {
			controller.edit()
		}
		assert null == response.redirectedUrl
		assert 200 == response.status
		assert dummyCredential == session.googleCredential
	}
}
