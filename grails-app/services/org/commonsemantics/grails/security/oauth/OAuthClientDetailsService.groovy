package org.commonsemantics.grails.security.oauth;

import org.commonsemantics.grails.systems.model.SystemApi;
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Class to handle the checking of the client id sent by the client against
 * the registered systems in the database.
 * @author Tom Wilkin */
class OAuthClientDetailsService implements ClientDetailsService {
	
	def grailsApplication;

	@Override
	public ClientDetails loadClientByClientId(final String clientId) throws ClientRegistrationException {
		// check if the passed client id is actually the username (overcome a bug)
		String requestClientId = getRequestClientId( );
		if(requestClientId == null) {
			requestClientId = SecurityContextHolder.getContext( ).getAuthentication( ).getName( );
		}
		if(!clientId.equals(requestClientId)) {
			// override the username and search using the actual client id
			BaseClientDetails client = findSystemApiByClientId(requestClientId);
			return client;
		}
		
		// find the SystemApi by the client id and return it
		ClientDetails client = findSystemApiByClientId(clientId);
		return client;
	}
	
	/** Create a new BaseClientDetails object from the SystemApi in the database.
	 * @param clientId The client id to search for.
	 * @return The new BaseClientDetails object created from the SystemApi, or null if it could not be found. */
	private BaseClientDetails findSystemApiByClientId(final String clientId) throws ClientRegistrationException {
		SystemApi system = SystemApi.findByShortName(clientId);
		if(system != null) {
			// ensure the system is enabled
			if(!system.isEnabled( )) {
				throw new NoSuchClientException("The client with requested id '" + clientId + "' is disabled.");
			}
			
			BaseClientDetails client = new BaseClientDetails( );
			client.setClientId(system.getShortName( ))
			client.setClientSecret(system.getSecretkey( ));
			client.setAccessTokenValiditySeconds(grailsApplication.config.grails.plugin.springsecurity.oauthProvider.defaultClientConfig.accessTokenValiditySeconds);
			client.setRefreshTokenValiditySeconds(grailsApplication.config.grails.plugin.springsecurity.oauthProvider.defaultClientConfig.refreshTokenValiditySeconds);
			
			// add the default grants
			Collection<String> grants = new ArrayList<String>( );
			for(String grant : grailsApplication.config.grails.plugin.springsecurity.oauthProvider.defaultClientConfig.authorizedGrantTypes) {
				grants.add(grant);
			}
			client.setAuthorizedGrantTypes(grants);
			
			// add the authorities
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(1);
			authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
			client.setAuthorities(authorities);
			
			return client;
		}
		
		throw new NoSuchClientException("No such client exists with requested id '" + clientId + "'.");
	}
	
	/** Extract the client id value from the original request.
	 * @return The client id extracted from the original request. */
	private String getRequestClientId( ) {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes( );
		String queryStr = (String)attributes.getAttribute("javax.servlet.forward.query_string", RequestAttributes.SCOPE_REQUEST);
		String[ ] parts = queryStr.split("&");
		for(String s : parts) {
			if(s.contains("client_id")) {
				parts = s.split("=");
				return parts[1].trim( );
			}
		}
		
		return null;
	}
	
};
