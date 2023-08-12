package dyamo.narek.syntechnica.security;

import dyamo.narek.syntechnica.tokens.access.AccessTokenConfigurationProperties;
import dyamo.narek.syntechnica.tokens.family.TokenGenerationValidityCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidFamilyJwtAuthenticationProvider implements AuthenticationProvider {

	private final VersionedJwtAuthenticationProvider nextProvider;

	private final TokenGenerationValidityCoordinator tokenGenerationValidityCoordinator;

	private final AccessTokenConfigurationProperties accessTokenProperties;


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		var authToken = (JwtAuthenticationToken) nextProvider.authenticate(authentication);
		Jwt jwt = authToken.getToken();

		long familyId = jwt.getClaim(accessTokenProperties.getClaims().getFamily());
		long generation = jwt.getClaim(accessTokenProperties.getClaims().getGeneration());

		long lastGeneration = tokenGenerationValidityCoordinator.getTokenFamilyLastGeneration(familyId).orElseThrow(
				() -> new InvalidBearerTokenException("An error occurred while attempting to validate the Jwt:" +
						" Defunct family"));

		if (generation != lastGeneration) {
			tokenGenerationValidityCoordinator.invalidateTokenFamily(familyId);
			throw new InvalidBearerTokenException("An error occurred while attempting to validate the Jwt:" +
					" Prohibited, not the last generation");
		}

		return authToken;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return nextProvider.supports(authentication);
	}
}
