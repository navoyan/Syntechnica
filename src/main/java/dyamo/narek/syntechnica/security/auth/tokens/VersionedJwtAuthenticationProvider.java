package dyamo.narek.syntechnica.security.auth.tokens;

import dyamo.narek.syntechnica.security.auth.tokens.access.AccessTokenConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VersionedJwtAuthenticationProvider implements AuthenticationProvider {

	private final JwtAuthenticationProvider originalProvider;

	private final AccessTokenVersionProvider versionProvider;

	private final AccessTokenConfigurationProperties accessTokenProperties;


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		var authToken = (JwtAuthenticationToken) originalProvider.authenticate(authentication);
		Jwt jwt = authToken.getToken();

		long version = jwt.getClaim(accessTokenProperties.getClaims().getVersion());

		if (version != versionProvider.getAccessTokenCurrentVersion(jwt.getSubject())) {
			throw new InvalidBearerTokenException("An error occurred while attempting to validate the Jwt: Outdated version");
		}

		return authToken;
	}


	@Override
	public boolean supports(Class<?> authentication) {
		return originalProvider.supports(authentication);
	}

}
