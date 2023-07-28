package dyamo.narek.syntechnica.security.auth.tokens;

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
	TokenService tokenService;

	JwtConfigurationProperties jwtProperties;


	VersionedJwtAuthenticationProvider authenticationProvider;


	@BeforeEach
	void beforeEach() {
		jwtProperties = configProperties(JwtConfigurationProperties.class);

		authenticationProvider = new VersionedJwtAuthenticationProvider(
				originalProvider, tokenService, jwtProperties
		);
	}


	@Test
	void authenticate_shouldReturnOriginallyProvidedAuthentication_whenVersionMatches() {
		String username = "user";
		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject(username)
				.claim(jwtProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(jwtProperties.getClaims().getVersion(), 5L)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(originalProvider.authenticate(authentication)).willReturn(authToken);
		given(tokenService.getAccessTokenCurrentVersion(username)).willReturn(5L);


		Authentication resultAuthentication = authenticationProvider.authenticate(authentication);


		assertThat(resultAuthentication).isEqualTo(authToken);
	}

	@Test
	void authenticate_shouldThrowException_whenVersionDoesNotMatch() {
		String username = "user";
		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject(username)
				.claim(jwtProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(jwtProperties.getClaims().getVersion(), 4L)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(originalProvider.authenticate(authentication)).willReturn(authToken);
		given(tokenService.getAccessTokenCurrentVersion(username)).willReturn(5L);


		Exception thrown = catchException(() -> {
			authenticationProvider.authenticate(authentication);
		});


		assertThat(thrown).isInstanceOf(InvalidBearerTokenException.class);
	}
}