package org.commonsemantics.grails.security.oauth.controllers

import groovy.json.JsonOutput

class IdentityController {

	def apiKeyAuthenticationService

	def apiKey, user

	def beforeInterceptor = {
		// Authenticate with OAuth
		def oauthToken = apiKeyAuthenticationService.getOauthToken(request)
		if(oauthToken != null) {
			apiKey = oauthToken.system.apikey
			user = oauthToken.user
			if(user == null) {
				render(status: 404, text: JsonOutput.toJson([message: "User not found"]),
					contentType: "text/json", encoding: "UTF-8")
				return
			}
		} else { // OAuth authentication from CsOauth should prevent this branch being reached
			render(status: 404, text: JsonOutput.toJson([message: "Missing or invalid OAuth token"]),
				contentType: "text/json", encoding: "UTF-8")
			return
		}
		log.info("API key [" + apiKey + "]")
		log.info("User identified as [" + user.person.displayName + "]")
	}

	// To return information on which user was authenticated with the given OAuth token
	def whoami = {
		render(status: 200, text: JsonOutput.toJson([id: user.id, name: user.person.displayName]),
			contentType: "text/json", encoding: "UTF-8")
		return
	}
}
