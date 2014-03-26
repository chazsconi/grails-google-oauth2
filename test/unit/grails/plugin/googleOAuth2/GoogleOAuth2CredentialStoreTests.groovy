package grails.plugin.googleOAuth2

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class GoogleOAuth2CredentialStoreTests {

    void testInstantiateStore() {
        def store = new GoogleOAuth2CredentialStore(userRef: "test@test.com")
    }
}
