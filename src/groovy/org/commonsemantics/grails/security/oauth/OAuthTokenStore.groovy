package org.commonsemantics.grails.security.oauth

import java.util.Collection;
import org.commonsemantics.grails.systems.model.SystemApi;
import org.commonsemantics.grails.users.model.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;

/** Class to store the authorised access and refresh tokens in the database
 * referencing both the user the token authorises and the system API that can
 * use it.
 * @author Tom Wilkin */
class OAuthTokenStore implements TokenStore {

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(final String clientId) {
		// find the system
		SystemApi system = SystemApi.findByShortName(clientId);
		
		// find and add the access tokens
		OAuthStoredAccessToken[ ] results = OAuthStoredAccessToken.findAllBySystem(system);
		Collection<OAuth2AccessToken> tokens = new ArrayList<OAuth2AccessToken>( );
		for(OAuthStoredAccessToken token : results) {
			OAuth2AccessToken accessToken = createAccessToken(token);
			if(accessToken != null) {
				tokens.add(accessToken);
			}
		}
		
		return tokens;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByUserName(final String username) {
		// find the user
		User user = User.findByUserame(username);
		
		// find and add the access tokens
		OAuthStoredAccessToken[ ] results = OAuthStoredAccessToken.findAllByUser(user);
		Collection<OAuth2AccessToken> tokens = new ArrayList<OAuth2AccessToken>( );
		for(OAuthStoredAccessToken token : results) {
			OAuth2AccessToken accessToken = createAccessToken(token);
			if(accessToken != null) {
				tokens.add(accessToken);
			}
		}
		
		return tokens;
	}

	@Override
	public OAuth2AccessToken getAccessToken(final OAuth2Authentication authentication) {
		// search the database for the links
		User user = User.findByUsername(authentication.getName( ));
		SystemApi system = SystemApi.findByShortName(authentication.getAuthorizationRequest( ).getClientId( ));
		
		// find the access token
		OAuthStoredAccessToken accessToken = OAuthStoredAccessToken.findBySystemAndUser(system, user);
		return createAccessToken(accessToken);
	}

	@Override
	public OAuth2AccessToken readAccessToken(final String tokenValue) {
		// search for the access token
		OAuthStoredAccessToken token = OAuthStoredAccessToken.findByToken(tokenValue);
		return createAccessToken(token);
	}

	@Override
	public OAuth2Authentication readAuthentication(final OAuth2AccessToken token) {
		if(token != null) {
			return readAuthentication(token.getValue( ));
		} else {
			return null;
		}
	}

	@Override
	public OAuth2Authentication readAuthentication(final String token) {
		// find the access token
		OAuthStoredAccessToken accessToken = OAuthStoredAccessToken.findByToken(token);
		if(accessToken == null) {
			return null;
		}
		
		// extract the OAuth2Authentication
		return SerializationUtils.deserialize(accessToken.getAuthentication( ));
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(final OAuth2RefreshToken token) {
		// find the refresh token
		OAuthStoredRefreshToken refreshToken = OAuthStoredRefreshToken.findByToken(token.getValue( ));
		if(refreshToken == null) {
			return null;
		}
		
		// extract the OAuth2Authentication
		return SerializationUtils.deserialize(refreshToken.getAccessToken( ).getAuthentication( ));
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(final String tokenValue) {
		// search for the refresh token
		OAuthStoredRefreshToken token = OAuthStoredRefreshToken.findByToken(tokenValue);
		if(token == null) {
			return null;
		}
		
		return createRefreshToken(token);
	}

	@Override
	public void removeAccessToken(final OAuth2AccessToken tokenValue) {
		OAuthStoredAccessToken token = OAuthStoredAccessToken.findByToken(tokenValue);
		if(token != null) {
			token.delete( );
		}
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(final OAuth2RefreshToken token) {
		OAuthStoredRefreshToken refreshToken = OAuthStoredRefreshToken.findByToken(token.getValue( ));
		if(refreshToken != null) {
			OAuthStoredAccessToken accessToken = refreshToken.getAccessToken( );
			if(accessToken != null) {
				accessToken.delete( );
				refreshToken.delete( );
			}
		}
	}

	@Override
	public void removeRefreshToken(final OAuth2RefreshToken token) {
		OAuthStoredRefreshToken refreshToken = OAuthStoredRefreshToken.findByToken(token.getValue( ))
		if(refreshToken != null) {
			refreshToken.delete( );
		}		
	}

	@Override
	@Transactional
	public void storeAccessToken(final OAuth2AccessToken token, final OAuth2Authentication authentication) {
		// delete the token if it already exists
		if(readAccessToken(token.getValue( ))) {
			removeAccessToken(token);
		}
		
		// search the database for the links
		User user = User.findByUsername(authentication.getName( ));
		SystemApi system = SystemApi.findByShortName(authentication.getAuthorizationRequest( ).getClientId( ));
		
		// create the database token
		OAuthStoredAccessToken dbToken = new OAuthStoredAccessToken(
			user: user,
			system: system,
			token: token.getValue( ),
			expiration: token.getExpiration( ),
			authentication: SerializationUtils.serialize(authentication)
		);
	
		// store the new token
		if(!dbToken.save(flush: true)) {
			System.out.println(dbToken.errors.allErrors);
		}
		
		// store the refresh token
		if(token.getRefreshToken( ) != null) {
			storeRefreshToken(token.getRefreshToken( ), authentication);
		}
	}
	
	@Override
	@Transactional
	public void storeRefreshToken(final OAuth2RefreshToken token, final OAuth2Authentication authentication) {
		// find the access token
		User user = User.findByUsername(authentication.getName( ));
		SystemApi system = SystemApi.findByShortName(authentication.getAuthorizationRequest( ).getClientId( ));
		OAuthStoredAccessToken accessToken = OAuthStoredAccessToken.findBySystemAndUser(system, user);
		storeRefreshToken(token, accessToken, system, user);
	}

	public void storeRefreshToken(final OAuth2RefreshToken token, final OAuthStoredAccessToken accessToken,
		final SystemApi system, final User user)
	{
		// delete the token if it already exists
		if(readRefreshToken(token.getValue( ))) {
			removeRefreshToken(token);
		}
		
		// create the database token
		OAuthStoredRefreshToken dbToken = new OAuthStoredRefreshToken(
			user: user,
			system: system,
			token: token.getValue( ),
			accessToken: accessToken
		);
	
		// store the new token
		if(!dbToken.save(flush: true)) {
			System.out.println(dbToken.errors.allErrors);
		}		
	}
			
	/** Create an OAuth2AccessToken from the database token.
	 * @param dbToken The database token.
	 * @return The OAuth2AccessToken. */
	private OAuth2AccessToken createAccessToken(final OAuthStoredAccessToken dbToken) {
		// check for null
		if(dbToken == null) {
			return null;
		}
		
		DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(dbToken.getToken( ));
		token.setExpiration(dbToken.getExpiration( ));
		
		// find refresh token
		OAuthStoredRefreshToken refresh = OAuthStoredRefreshToken.findByAccessToken(dbToken);
		if(refresh != null) {
			token.setRefreshToken(createRefreshToken(refresh));
		}
		
		return token;
	}
	
	/** Create an OAuth2RefreshToken from the database token.
	 * @param dbToken The database token.
	 * @return The OAuth2RefreshToken. */
	private OAuth2RefreshToken createRefreshToken(final OAuthStoredRefreshToken dbToken) {
		DefaultOAuth2RefreshToken token = new DefaultOAuth2RefreshToken(dbToken.getToken( ));
		return token;
	}

};
