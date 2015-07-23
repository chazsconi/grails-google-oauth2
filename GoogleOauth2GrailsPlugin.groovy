// The name of the plugin is converted from this class name, hence this will be google-oauth2.  Thus we cannot have GoogleOAuth2
class GoogleOauth2GrailsPlugin {
	def version = "0.17"
	def grailsVersion = "2.0 > *"
	def title = "Google Oauth2 Plugin"
	def description = '''\
Provides integration with Google Accounts to use with services such as Google Calendar.
Handles the OAuth2 control flow including redirecting the user to Google to request authorisation,
storing the credential in the session and DB (encrypted) and obtaining refresh tokens when necessary.
'''
	def documentation = "http://grails.org/plugin/google-oauth2"
	def license = "GPL3"
	def developers = [
		[name: 'Charles Bernasconi', email: 'grails-google-oauth2@sconi.net']
	]
	def issueManagement = [system: 'GITHUB', url: 'https://github.com/chazsconi/grails-google-oauth2/issues']
	def scm = [url: 'https://github.com/chazsconi/grails-google-oauth2']
}
