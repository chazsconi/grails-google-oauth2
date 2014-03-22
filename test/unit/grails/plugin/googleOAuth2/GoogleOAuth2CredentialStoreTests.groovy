package grails.plugin.googleOAuth2

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class GoogleOAuth2CredentialStoreTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testInstantiateStore() {
        def store = new GoogleOAuth2CredentialStore(userRef: "test@test.com")
    }
}
