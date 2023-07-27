package dyamo.narek.syntechnica.security.auth.tokens;

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
class TokenServiceTests {

	JwtConfigurationProperties properties;
	@Mock
	JwtEncoder jwtEncoder;

	TokenService tokenService;


	@Captor
	ArgumentCaptor<JwtEncoderParameters> jwtEncoderParametersCaptor;


	@BeforeEach
	void beforeEach() {
		properties = configProperties(JwtConfigurationProperties.class);
		tokenService = new TokenService(properties, jwtEncoder);
	}


	@Test
	void createAccessToken_shouldCreateValidJwt() {
		User user = user().withAuthorities(
				authority().withType(UserAuthorityType.ADMIN).build(),
				authority().withType(UserAuthorityType.READ).withScope("*").build()
		).build();

		Jwt jwtMock = mock(Jwt.class);
		given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwtMock);
		given(jwtMock.getTokenValue()).willReturn("ENCODED JWT");


		tokenService.createAccessToken(user);


		verify(jwtEncoder).encode(jwtEncoderParametersCaptor.capture());
		JwtEncoderParameters passedJwtParameters = jwtEncoderParametersCaptor.getValue();

		assertThat(passedJwtParameters.getClaims()).satisfies(claims -> {
			assertThat(claims.getClaimAsString(JwtClaimNames.ISS)).isEqualTo("syntechnica");
			assertThat(claims.getIssuedAt().plus(properties.getExpirationTime())).isEqualTo(claims.getExpiresAt());
			assertThat(claims.getSubject()).isEqualTo(user.getName());
			assertThat(claims.getClaimAsStringList("authorities")).containsExactlyInAnyOrderElementsOf(
					user.getAuthorities().stream().map(UserAuthority::getAuthority).collect(Collectors.toList())
			);
		});
	}
}