package dyamo.narek.syntechnica.security.auth.tokens.access;

import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.stream.Collectors;

import static dyamo.narek.syntechnica.global.ConfigurationPropertyHolders.configProperties;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static dyamo.narek.syntechnica.users.authorities.TestUserAuthorityBuilder.authority;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTests {

	AccessTokenConfigurationProperties accessTokenProperties;

	@Mock
	JwtEncoder jwtEncoder;
	@Mock
	AccessTokenMetadataRepository accessTokenMetadataRepository;


	AccessTokenService accessTokenService;


	@Captor
	ArgumentCaptor<JwtEncoderParameters> jwtEncoderParametersCaptor;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
		accessTokenProperties = configProperties(AccessTokenConfigurationProperties.class);
		accessTokenService = new AccessTokenService(accessTokenProperties, jwtEncoder, accessTokenMetadataRepository);
	}


	@Test
	void createAccessToken_shouldCreateValidJwtWithSavedVersion_whenTokenMetadataIsPersisted() {
		User user = user().withId().withAuthorities(
				authority().withType(UserAuthorityType.ADMIN).build(),
				authority().withType(UserAuthorityType.READ).withScope("*").build()
		).build();

		long tokenVersion = 5L;
		var metadata = AccessTokenMetadata.builder()
				.userId(user.getId()).user(user)
				.version(tokenVersion)
				.build();
		given(accessTokenMetadataRepository.findById(user.getId())).willReturn(Optional.of(metadata));

		Jwt jwtMock = mock(Jwt.class);
		given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwtMock);
		given(jwtMock.getTokenValue()).willReturn("ENCODED JWT");


		accessTokenService.createAccessToken(user);


		verify(jwtEncoder).encode(jwtEncoderParametersCaptor.capture());
		JwtEncoderParameters passedJwtParameters = jwtEncoderParametersCaptor.getValue();

		assertThat(passedJwtParameters.getClaims()).satisfies(claims -> {
			assertThat(claims.getClaimAsString(JwtClaimNames.ISS)).isEqualTo(accessTokenProperties.getIssuer());
			assertThat(claims.getIssuedAt().plus(accessTokenProperties.getExpirationTime())).isEqualTo(claims.getExpiresAt());
			assertThat(claims.getSubject()).isEqualTo(user.getName());
			assertThat(claims.getClaimAsStringList(accessTokenProperties.getClaims().getAuthorities())).containsExactlyInAnyOrderElementsOf(
					user.getAuthorities().stream().map(UserAuthority::getAuthority).collect(Collectors.toList())
			);
			assertThat(claims.<Long>getClaim(accessTokenProperties.getClaims().getVersion())).isEqualTo(tokenVersion);
		});
	}

	@Test
	void createAccessToken_shouldCreateValidJwtWithVersion1_whenTokenMetadataIsNotPersisted() {
		User user = user().withId().withAuthorities(
				authority().withType(UserAuthorityType.ADMIN).build(),
				authority().withType(UserAuthorityType.READ).withScope("*").build()
		).build();

		given(accessTokenMetadataRepository.findById(user.getId())).willReturn(Optional.empty());

		Jwt jwtMock = mock(Jwt.class);
		given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwtMock);
		given(jwtMock.getTokenValue()).willReturn("ENCODED JWT");


		accessTokenService.createAccessToken(user);


		verify(jwtEncoder).encode(jwtEncoderParametersCaptor.capture());
		JwtEncoderParameters passedJwtParameters = jwtEncoderParametersCaptor.getValue();

		assertThat(passedJwtParameters.getClaims()).satisfies(claims -> {
			assertThat(claims.getClaimAsString(JwtClaimNames.ISS)).isEqualTo(accessTokenProperties.getIssuer());
			assertThat(claims.getIssuedAt().plus(accessTokenProperties.getExpirationTime())).isEqualTo(claims.getExpiresAt());
			assertThat(claims.getSubject()).isEqualTo(user.getName());
			assertThat(claims.getClaimAsStringList(accessTokenProperties.getClaims().getAuthorities())).containsExactlyInAnyOrderElementsOf(
					user.getAuthorities().stream().map(UserAuthority::getAuthority).collect(Collectors.toList())
			);
			assertThat(claims.<Long>getClaim(accessTokenProperties.getClaims().getVersion())).isEqualTo(1L);
		});
	}


	@Test
	void getAccessTokenCurrentVersion_shouldReturnCurrentVersion_whenMetadataExists() {
		String username = "user";
		int persistedVersion = 5;

		given(accessTokenMetadataRepository.findByUsername(username)).willReturn(
				Optional.of(AccessTokenMetadata.builder().version(persistedVersion).build())
		);


		long providedVersion = accessTokenService.getAccessTokenCurrentVersion(username);


		assertThat(providedVersion).isEqualTo(persistedVersion);
	}

	@Test
	void getAccessTokenCurrentVersion_shouldReturn1_whenMetadataNotExists() {
		String username = "user";

		given(accessTokenMetadataRepository.findByUsername(username)).willReturn(Optional.empty());


		long providedVersion = accessTokenService.getAccessTokenCurrentVersion(username);


		assertThat(providedVersion).isEqualTo(1);
	}

}