package dyamo.narek.syntechnica.security;

import dyamo.narek.syntechnica.tokens.access.AccessTokenConfigurationProperties;
import dyamo.narek.syntechnica.tokens.access.AccessTokenVersionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static dyamo.narek.syntechnica.global.ConfigurationPropertyHolders.configProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class VersionedJwtAuthenticationProviderTests {

	@Mock
	JwtAuthenticationProvider originalProvider;
	@Mock
	AccessTokenVersionProvider accessTokenVersionProvider;

	AccessTokenConfigurationProperties accessTokenProperties;


	VersionedJwtAuthenticationProvider authenticationProvider;


	@BeforeEach
	void beforeEach() {
		accessTokenProperties = configProperties(AccessTokenConfigurationProperties.class);

		authenticationProvider = new VersionedJwtAuthenticationProvider(
				originalProvider, accessTokenVersionProvider, accessTokenProperties
		);
	}


	@Test
	void authenticate_shouldReturnOriginallyProvidedAuthentication_whenVersionMatches() {
		String username = "user";
		long tokenVersion = 5L;

		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(accessTokenProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject(username)
				.claim(accessTokenProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(accessTokenProperties.getClaims().getVersion(), tokenVersion)
				.claim(accessTokenProperties.getClaims().getFamily(), 10L)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(originalProvider.authenticate(authentication)).willReturn(authToken);
		given(accessTokenVersionProvider.getAccessTokenCurrentVersion(username))
				.willReturn(tokenVersion);


		Authentication resultAuthentication = authenticationProvider.authenticate(authentication);


		assertThat(resultAuthentication).isEqualTo(authToken);
	}

	@Test
	void authenticate_shouldThrowException_whenVersionDoesNotMatch() {
		String username = "user";
		long tokenVersion = 4L;

		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(accessTokenProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject(username)
				.claim(accessTokenProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(accessTokenProperties.getClaims().getVersion(), tokenVersion)
				.claim(accessTokenProperties.getClaims().getFamily(), 10L)
				.claim(accessTokenProperties.getClaims().getGeneration(), 1L)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(originalProvider.authenticate(authentication)).willReturn(authToken);
		given(accessTokenVersionProvider.getAccessTokenCurrentVersion(username)).willReturn(5L);


		Exception thrown = catchException(() -> {
			authenticationProvider.authenticate(authentication);
		});


		assertThat(thrown).isInstanceOf(InvalidBearerTokenException.class);
	}

}