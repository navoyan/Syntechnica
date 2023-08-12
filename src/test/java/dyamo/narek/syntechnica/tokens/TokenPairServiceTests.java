package dyamo.narek.syntechnica.tokens;

import dyamo.narek.syntechnica.tokens.access.AccessTokenService;
import dyamo.narek.syntechnica.tokens.family.TokenFamily;
import dyamo.narek.syntechnica.tokens.family.TokenFamilyService;
import dyamo.narek.syntechnica.tokens.refresh.InvalidRefreshTokenException;
import dyamo.narek.syntechnica.tokens.refresh.ProhibitedRefreshTokenException;
import dyamo.narek.syntechnica.tokens.refresh.RefreshToken;
import dyamo.narek.syntechnica.tokens.refresh.RefreshTokenService;
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
class TokenPairServiceTests {

	@Mock
	UserService userService;
	@Mock
	TokenFamilyService tokenFamilyService;
	@Mock
	AccessTokenService accessTokenService;
	@Mock
	RefreshTokenService refreshTokenService;

	@InjectMocks
	TokenPairService tokenPairService;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void generateTokens_shouldGenerateValidTokenPair_whenProvidedCredentialsAreValid() {
		User user = user().withId().build();

		var tokenFamily = TokenFamily.builder().id(1L).user(user).build();
		given(tokenFamilyService.createTokenFamily(user)).willReturn(tokenFamily);

		var credentials = new UserCredentialsRequest(user.getName(), "password");

		given(userService.findUserByName(user.getName())).willReturn(Optional.of(user));
		given(userService.isUserMatchingPassword(user, credentials.getPassword())).willReturn(true);

		String encodedAccessToken = "ENCODED ACCESS TOKEN";
		UUID refreshTokenValue = UUID.randomUUID();
		given(accessTokenService.createAccessToken(tokenFamily)).willReturn(encodedAccessToken);
		given(refreshTokenService.createRefreshToken(tokenFamily)).willReturn(refreshTokenValue);


		TokenPairResponse tokenPairResponse = tokenPairService.generateTokens(credentials);


		assertThat(tokenPairResponse.getAccessToken()).isEqualTo(encodedAccessToken);
		assertThat(tokenPairResponse.getRefreshToken()).isEqualTo(refreshTokenValue);
	}

	@Test
	void generateTokens_shouldThrowException_whenUserDoesntExist() {
		var credentials = new UserCredentialsRequest("user", "password");

		given(userService.findUserByName(credentials.getUsername())).willReturn(Optional.empty());


		Exception thrown = catchException(() -> {
			tokenPairService.generateTokens(credentials);
		});


		assertThat(thrown).isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void generateTokens_shouldThrowException_whenUserPasswordDoesNotMatch() {
		User user = user().withId().build();
		var credentials = new UserCredentialsRequest(user.getName(), "password");

		given(userService.findUserByName(user.getName())).willReturn(Optional.of(user));
		given(userService.isUserMatchingPassword(user, credentials.getPassword())).willReturn(false);


		Exception thrown = catchException(() -> {
			tokenPairService.generateTokens(credentials);
		});


		assertThat(thrown).isInstanceOf(InvalidCredentialsException.class);
	}


	@Test
	void generateTokens_shouldGenerateValidTokenPair_whenProvidedRefreshTokenIsValid() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.lastGeneration(1L)
				.user(user().withId().build())
				.build();

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(providedRefreshTokenValue)
				.family(tokenFamily)
				.generation(tokenFamily.getLastGeneration())
				.creationTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
				.expirationTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES))
				.build();
		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.of(persistedRefreshToken));

		String encodedAccessToken = "ENCODED ACCESS TOKEN";
		UUID newRefreshTokenValue = UUID.randomUUID();
		given(accessTokenService.createAccessToken(tokenFamily)).willReturn(encodedAccessToken);
		given(refreshTokenService.createRefreshToken(tokenFamily)).willReturn(newRefreshTokenValue);


		TokenPairResponse tokenPairResponse = tokenPairService.generateTokens(providedRefreshTokenValue);


		verify(tokenFamilyService, never()).invalidateTokenFamily(tokenFamily);

		assertThat(tokenPairResponse.getAccessToken()).isEqualTo(encodedAccessToken);
		assertThat(tokenPairResponse.getRefreshToken()).isEqualTo(newRefreshTokenValue);
	}

	@Test
	void generateTokens_shouldThrowException_whenNoPersistedRefreshTokenIsFound() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.empty());


		Exception thrown = catchException(() -> {
			tokenPairService.generateTokens(providedRefreshTokenValue);
		});


		assertThat(thrown).isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	void generateTokens_shouldInvalidateTokenFamilyAndThrowException_whenProvidedRefreshTokenIsNotLastGeneration() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.lastGeneration(2L)
				.user(user().withId().build())
				.build();

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(providedRefreshTokenValue)
				.family(tokenFamily)
				.generation(1L)
				.creationTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
				.expirationTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES))
				.build();
		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.of(persistedRefreshToken));


		Exception thrown = catchException(() -> {
			tokenPairService.generateTokens(providedRefreshTokenValue);
		});


		verify(tokenFamilyService).invalidateTokenFamily(tokenFamily);

		assertThat(thrown).isInstanceOf(ProhibitedRefreshTokenException.class);
	}

	@Test
	void generateTokens_shouldThrowException_whenProvidedRefreshTokenIsExpired() {
		UUID providedRefreshTokenValue = UUID.randomUUID();

		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.lastGeneration(1L)
				.user(user().withId().build())
				.build();

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(providedRefreshTokenValue)
				.family(tokenFamily)
				.generation(tokenFamily.getLastGeneration())
				.creationTimestamp(Instant.now().minus(2, ChronoUnit.HOURS))
				.expirationTimestamp(Instant.now().minus(1, ChronoUnit.HOURS))
				.build();
		given(refreshTokenService.findRefreshTokenByValue(providedRefreshTokenValue))
				.willReturn(Optional.of(persistedRefreshToken));


		Exception thrown = catchException(() -> {
			tokenPairService.generateTokens(providedRefreshTokenValue);
		});


		verify(tokenFamilyService, never()).invalidateTokenFamily(tokenFamily);

		assertThat(thrown).isInstanceOf(InvalidRefreshTokenException.class);
	}

}