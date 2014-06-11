package org.commonsemantics.grails.security.oauth

/** Domain class modelling the access tokens used to authenticate users with OAuth.
 * @author Tom Wilkin */
class OAuthStoredAccessToken extends OAuthToken {

	/** The serialised authentication object. */
	byte[ ] authentication;
	
	static constraints = {
		authentication blank: false, maxSize: 65535
	}
	
};
