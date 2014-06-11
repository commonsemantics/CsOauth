package org.commonsemantics.grails.security.oauth

import java.util.Date;

import org.commonsemantics.grails.systems.model.SystemApi;
import org.commonsemantics.grails.users.model.User;

/** Domain class modelling the access and refresh tokens used to 
 * authenticate users with OAuth.
 * @author Tom Wilkin */
abstract class OAuthToken {

    protected static final int ID_MAX_SIZE = 36;

	/** Artificial id for the OAuth Tokens. */
	String id;
	
	/** The user the token authenticates. */
	User user;
	
	/** The application the token is used by. */
	SystemApi system;
	
	/** The actual authentication token. */
	String token;
	
	/** The expiration date for the access token. */
	Date expiration;
	
	static constraints = {
		id maxSize: ID_MAX_SIZE
	
		token blank: false, unique: true, maxSize: ID_MAX_SIZE
		expiration nullable: true
	}
	
	static mapping = {
		id generator:'uuid', sqlType: "varchar(36)"
	}
	
};

