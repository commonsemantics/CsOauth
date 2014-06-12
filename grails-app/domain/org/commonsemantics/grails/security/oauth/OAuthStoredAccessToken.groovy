package org.commonsemantics.grails.security.oauth

import java.util.Date;

/** Domain class modelling the access tokens used to authenticate users with OAuth.
 * @author Tom Wilkin */
class OAuthStoredAccessToken extends OAuthToken {

	/** The expiration date for the access token. */
	Date expiration;
	
	static constraints = {
		expiration nullable: true
	}
	
};
