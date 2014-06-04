package org.commonsemantics.grails.security.oauth

/** Domain class modelling the access tokens used to authenticate users with OAuth.
 * @author Tom Wilkin */
class OAuthStoredAccessToken extends OAuthToken {
	
	/** The expiration date for the access token. */
	Date expiration;

	/** The serialised authentication object. */
	byte[ ] authentication;
	
	static constraints = {
		expiration blank: false
		authentication blank: false, maxSize: 65535
	}
	
};
