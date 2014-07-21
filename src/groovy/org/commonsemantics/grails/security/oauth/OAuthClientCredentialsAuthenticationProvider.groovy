package org.commonsemantics.grails.security.oauth;

import org.commonsemantics.grails.systems.model.SystemApi;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator.UnauthorizedException;

/** Authentication Provider to catch the client credentials authentication during the user
 * access token creation procedure.
 * @author Tom Wilkin */
class OAuthClientCredentialsAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(final Authentication authentication)
			throws AuthenticationException
	{
		// extract the HTTP Basic credentials
		String clientId = authentication.getName( );
		String secret = authentication.getCredentials( ).toString( );
		
		// find the System
		SystemApi system = SystemApi.findByShortName(clientId);
		if(system != null) {
			// ensure the System is enabled
			if(!system.isEnabled( )) {
				throw new NoSuchClientException("The client with requested id '" + clientId + "' is disabled.");
			}
			
			// check the secret is correct
			if(system.getSecretkey( ).equals(secret)) {
				List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>( );
				auths.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
				return new UsernamePasswordAuthenticationToken(clientId, secret, auths);
			}
		}
		throw new UnauthorizedException("Unable to authenticate with provided client id and secret.");
	}

	@Override
	public boolean supports(final Class<?> clazz) {
		return clazz.equals(UsernamePasswordAuthenticationToken.class);
	}

};
