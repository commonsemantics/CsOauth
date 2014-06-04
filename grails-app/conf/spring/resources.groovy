import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.commonsemantics.grails.security.oauth.OAuthClientDetailsService;
import org.commonsemantics.grails.security.oauth.OAuthTokenStore;

beans = {
	
	// OAuth
	clientDetailsService(OAuthClientDetailsService) {
		grailsApplication = ref("grailsApplication")
	}

	tokenStore(OAuthTokenStore)
	
	tokenServices(DefaultTokenServices) {
		tokenStore = ref("tokenStore")
		supportRefreshToken = "true"
		clientDetailsService = ref("clientDetailsService")
		accessTokenValiditySeconds = 86400
	}
	
}
