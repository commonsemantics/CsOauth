package org.commonsemantics.grails.security.oauth

/** Domain class modelling the refresh tokens used to authenticate users with OAuth.
 * @author Tom Wilkin */
class OAuthStoredRefreshToken extends OAuthToken {

	/** The access token this refresh token belongs to. */
	OAuthStoredAccessToken accessToken;

	static constraints = {
		accessToken nullable: true, blank: true, unique: true
	}

};
