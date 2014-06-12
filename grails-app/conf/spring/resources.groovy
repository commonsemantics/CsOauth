import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.commonsemantics.grails.security.oauth.OAuthAuthorizationCodeTokenGranter;
import org.commonsemantics.grails.security.oauth.OAuthClientDetailsService;
import org.commonsemantics.grails.security.oauth.OAuthTokenStore;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;

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
	
	authorizationCodeServices(InMemoryAuthorizationCodeServices)
	
	oauth2TokenGranter1(OAuthAuthorizationCodeTokenGranter,
			tokenServices = ref("tokenServices"),
			authorizationCodeServices = ref("authorizationCodeServices"),
			clientDetailsService = ref("clientDetailsService"))
	 
	oauth2TokenGranter2(RefreshTokenGranter,
			tokenServices = ref("tokenServices"),
			clientDetailsService = ref("clientDetailsService"))
	
	oauth2TokenGranter(CompositeTokenGranter, [ref("oauth2TokenGranter1"), ref("oauth2TokenGranter2")])
	
}
