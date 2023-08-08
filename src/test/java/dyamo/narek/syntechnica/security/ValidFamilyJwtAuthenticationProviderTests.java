package dyamo.narek.syntechnica.security;

import dyamo.narek.syntechnica.tokens.access.AccessTokenConfigurationProperties;
import dyamo.narek.syntechnica.tokens.family.TokenGenerationValidityCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dyamo.narek.syntechnica.global.ConfigurationPropertyHolders.configProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidFamilyJwtAuthenticationProviderTests {

	@Mock
	VersionedJwtAuthenticationProvider nextProvider;
	@Mock
	TokenGenerationValidityCoordinator tokenGenerationValidityCoordinator;

	AccessTokenConfigurationProperties accessTokenProperties;


	ValidFamilyJwtAuthenticationProvider authenticationProvider;


	@BeforeEach
	void beforeEach() {
		accessTokenProperties = configProperties(AccessTokenConfigurationProperties.class);

		authenticationProvider = new ValidFamilyJwtAuthenticationProvider(
				nextProvider, tokenGenerationValidityCoordinator, accessTokenProperties
		);
	}


	@Test
	void authenticate_shouldReturnOriginallyProvidedAuthentication_whenFamilyExistsAndLastGenerationTokenIsProvided() {
		long familyId = 5L;
		long generation = 1L;

		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(accessTokenProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject("user")
				.claim(accessTokenProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(accessTokenProperties.getClaims().getVersion(), 5L)
				.claim(accessTokenProperties.getClaims().getFamily(), familyId)
				.claim(accessTokenProperties.getClaims().getGeneration(), generation)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(nextProvider.authenticate(authentication)).willReturn(authToken);
		given(tokenGenerationValidityCoordinator.getTokenFamilyLastGeneration(familyId))
				.willReturn(Optional.of(generation));


		Authentication resultAuthentication = authenticationProvider.authenticate(authentication);


		assertThat(resultAuthentication).isEqualTo(authToken);
	}

	@Test
	void authenticate_shouldThrowException_whenFamilyDoesntExist() {
		long familyId = 5L;
		long generation = 1L;

		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(accessTokenProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject("user")
				.claim(accessTokenProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(accessTokenProperties.getClaims().getVersion(), 4L)
				.claim(accessTokenProperties.getClaims().getFamily(), familyId)
				.claim(accessTokenProperties.getClaims().getGeneration(), generation)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(nextProvider.authenticate(authentication)).willReturn(authToken);
		given(tokenGenerationValidityCoordinator.getTokenFamilyLastGeneration(familyId))
				.willReturn(Optional.empty());


		Exception thrown = catchException(() -> {
			authenticationProvider.authenticate(authentication);
		});


		assertThat(thrown).isInstanceOf(InvalidBearerTokenException.class);
	}

	@Test
	void authenticate_shouldThrowException_whenProvidedTokenIsNotTheLatestGeneration() {
		long familyId = 5L;
		long generation = 1L;

		Authentication authentication = mock(Authentication.class);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(accessTokenProperties.getIssuer())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject("user")
				.claim(accessTokenProperties.getClaims().getAuthorities(), List.of("ADMIN", "READ:*", "WRITE:*"))
				.claim(accessTokenProperties.getClaims().getVersion(), 4L)
				.claim(accessTokenProperties.getClaims().getFamily(), familyId)
				.claim(accessTokenProperties.getClaims().getGeneration(), generation)
				.build();

		Jwt jwt = new Jwt("ENCODED JWT",
				claims.getIssuedAt(), claims.getExpiresAt(),
				Map.of("alg", "RS256"), claims.getClaims()
		);
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);

		given(nextProvider.authenticate(authentication)).willReturn(authToken);

		long lastGeneration = 2L;
		given(tokenGenerationValidityCoordinator.getTokenFamilyLastGeneration(familyId))
				.willReturn(Optional.of(lastGeneration));


		Exception thrown = catchException(() -> {
			authenticationProvider.authenticate(authentication);
		});


		verify(tokenGenerationValidityCoordinator).invalidateTokenFamily(familyId);

		assertThat(thrown).isInstanceOf(InvalidBearerTokenException.class);
	}

}