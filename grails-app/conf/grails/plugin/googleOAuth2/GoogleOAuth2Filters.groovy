package grails.plugin.googleOAuth2

import javax.servlet.http.HttpServletRequest

import org.springframework.util.AntPathMatcher

class GoogleOAuth2Filters {

	def googleOAuth2Service
	AntPathMatcher matcher = new AntPathMatcher()

	def filters = {
		googleOAuth2(uri:"/**") {
			before = {
				Boolean storeCredentialInSession = grailsApplication.config.googleOAuth2.storeCredentialInSession
				log.debug "Filter URL=${requestUrl(request)}"
				def interceptUrlList = grailsApplication.config.googleOAuth2.interceptUrlList
				if(interceptUrlList.any { url -> matcher.match(url, request.forwardURI)})
				{
					log.info "In action requiring googleCredential session"
					if(!storeCredentialInSession || !session.googleCredential)
					{
						log.info "No googleCredential in session (or not storing in session).  Try to find credential in DB..."
						Closure getCurrentUserRef = grailsApplication.config.googleOAuth2.currentUserRef

						def googleCredential = googleOAuth2Service.loadCredential(getCurrentUserRef())
						if(!googleCredential) {
							log.info "No googleCredential found in DB.  Redirecting to authenticate on Google"

							// Remember the current URL so we can redirect back to it
							session.googleAuthSuccess = requestUrl(request)

							// Go back to the referer page in the event of a failure
							session.googleAuthFailure=request.getHeader('referer')

							redirect(controller: "googleOAuth2", action: "authorize")
							return false
						}
						if(storeCredentialInSession)
							session.googleCredential = googleCredential
					}
				}
			}
		}
	}

	String requestUrl(HttpServletRequest request)
	{
		StringBuilder sb = new StringBuilder(request.forwardURI)
		if(request.queryString) {
			sb << "?" << request.queryString
		}
		sb.toString()
	}
}
