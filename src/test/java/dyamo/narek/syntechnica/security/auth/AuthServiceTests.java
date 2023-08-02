package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.security.auth.tokens.access.AccessTokenService;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.InvalidRefreshTokenException;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.ProhibitedRefreshTokenException;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.RefreshToken;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.RefreshTokenService;
import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

	@Mock
	UserService userService;
	@Mock
	AccessTokenService accessTokenService;
	@Mock
	RefreshTokenService refreshTokenService;

	@InjectMocks
	AuthService authService;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void generateTokens_shouldGenerateValidTokenPair_whenProvidedCredentialsAreValid() {
		User user = user().build();
		var credentials = new UserCredentials(user.getName(), "password");

		given(userService.findUserByName(user.getName())).willReturn(Optional.of(user));
		given(userService.isUserMatchingPassword(user, credentials.getPassword())).willReturn(true);

		String encodedAccessToken = "ENCODED ACCESS TOKEN";
		UUID refreshTokenValue = UUID.randomUUID();
		given(accessTokenService.createAccessToken(user)).willReturn(encodedAccessToken);
		given(refreshTokenService.createRefreshToken(user)).willReturn(refreshTokenValue);


		TokenResponse tokenResponse = authService.generateTokens(credentials);


		assertThat(tokenResponse.getAccessToken()).isEqualTo(encodedAccessToken);
		assertThat(tokenResponse.getRefreshToken()).isEqualTo(refreshTokenValue);
	}

	@Test
	void generateTokens_shouldThrowException_whenUserDoesntExist() {
		var credentials = new UserCredentials("user", "password");

		given(userService.findUserByName(credentials.getUsername())).willReturn(Optional.empty());


		Exception thrown = catchException(() -> {
			authService.generateTokens(credentials);
		});


		assertThat(thrown).isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void generateTokens_shouldThrowException_whenUserPasswordDoesNotMatch() {
		User user = user().build();
		var credentials = new UserCredentials(user.getName(), "password");

		given(userService.findUserByName(user.getName())).willReturn(Optional.of(user));
		given(userService.isUserMatchingPassword(user, credentials.getPassword())).willReturn(false);


		Exception thrown = catchException(() -> {
			authService.generateTokens(credentials);
		});


		assertThat(thrown).isInstanceOf(InvalidCredentialsException.class);
	}


	@Test
	void generateTokens_shouldGenerateValidTokenPair_whenProvidedRefreshTokenIsValid() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		User user = user().withId().build();
		long family = 3L;

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(providedRefreshTokenValue)
				.family(family)
				.user(user)
				.creationTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
				.expirationTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES))
				.build();
		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.of(persistedRefreshToken));
		given(refreshTokenService.findCurrentAllowedRefreshToken(user, family))
				.willReturn(Optional.of(persistedRefreshToken));

		given(refreshTokenService.isRefreshTokenExpired(persistedRefreshToken)).willReturn(false);

		String encodedAccessToken = "ENCODED ACCESS TOKEN";
		UUID newRefreshTokenValue = UUID.randomUUID();
		given(accessTokenService.createAccessToken(user)).willReturn(encodedAccessToken);
		given(refreshTokenService.createRefreshToken(user, family)).willReturn(newRefreshTokenValue);


		TokenResponse tokenResponse = authService.generateTokens(providedRefreshTokenValue);


		verify(refreshTokenService).findCurrentAllowedRefreshToken(user, family);
		verify(refreshTokenService).isRefreshTokenExpired(persistedRefreshToken);
		verify(refreshTokenService, never()).invalidateUserRefreshTokenFamily(user, family);

		assertThat(tokenResponse.getAccessToken()).isEqualTo(encodedAccessToken);
		assertThat(tokenResponse.getRefreshToken()).isEqualTo(newRefreshTokenValue);
	}

	@Test
	void generateTokens_shouldThrowException_whenNoPersistedRefreshTokenIsFound() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.empty());


		Exception thrown = catchException(() -> {
			authService.generateTokens(providedRefreshTokenValue);
		});


		assertThat(thrown).isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void generateTokens_shouldInvalidateTokenFamilyAndThrowException_whenProvidedRefreshTokenIsNotCurrentAllowed() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		User user = user().withId().build();
		long family = 3L;

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(providedRefreshTokenValue)
				.family(family)
				.user(user)
				.creationTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
				.expirationTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES))
				.build();
		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.of(persistedRefreshToken));

		RefreshToken currentAllowedRefreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(family)
				.user(user)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();
		given(refreshTokenService.findCurrentAllowedRefreshToken(user, family))
				.willReturn(Optional.of(currentAllowedRefreshToken));


		Exception thrown = catchException(() -> {
			authService.generateTokens(providedRefreshTokenValue);
		});


		verify(refreshTokenService).findCurrentAllowedRefreshToken(user, family);
		verify(refreshTokenService).invalidateUserRefreshTokenFamily(user, family);

		assertThat(thrown).isInstanceOf(ProhibitedRefreshTokenException.class);
	}

	@Test
	void generateTokens_shouldThrowException_whenProvidedRefreshTokenIsExpired() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		User user = user().withId().build();
		long family = 3L;

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(providedRefreshTokenValue)
				.family(family)
				.user(user)
				.creationTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
				.expirationTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES))
				.build();
		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.of(persistedRefreshToken));
		given(refreshTokenService.findCurrentAllowedRefreshToken(user, family))
				.willReturn(Optional.of(persistedRefreshToken));

		given(refreshTokenService.isRefreshTokenExpired(persistedRefreshToken)).willReturn(true);


		Exception thrown = catchException(() -> {
			authService.generateTokens(providedRefreshTokenValue);
		});


		verify(refreshTokenService).findCurrentAllowedRefreshToken(user, family);
		verify(refreshTokenService).isRefreshTokenExpired(persistedRefreshToken);
		verify(refreshTokenService, never()).invalidateUserRefreshTokenFamily(user, family);

		assertThat(thrown).isInstanceOf(InvalidRefreshTokenException.class);
	}

}