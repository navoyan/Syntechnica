package dyamo.narek.syntechnica.security.auth.tokens.refresh;

import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static dyamo.narek.syntechnica.global.ConfigurationPropertyHolders.configProperties;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTests {

	@Mock
	RefreshTokenRepository refreshTokenRepository;

	RefreshTokenConfigurationProperties refreshTokenProperties;


	RefreshTokenService refreshTokenService;


	@Captor
	ArgumentCaptor<RefreshToken> refreshTokenCaptor;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();

		refreshTokenProperties = configProperties(RefreshTokenConfigurationProperties.class);
		refreshTokenService = new RefreshTokenService(
				refreshTokenRepository, refreshTokenProperties
		);
	}


	@Test
	void createRefreshToken_shouldCreateAndSaveValidRefreshTokenWithSpecifiedFamily() {
		User user = user().withId().build();
		long family = 3L;


		UUID createdRefreshTokenValue = refreshTokenService.createRefreshToken(user, family);


		verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
		RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

		assertThat(savedRefreshToken.getValue()).isEqualTo(createdRefreshTokenValue);
		assertThat(savedRefreshToken.getUser()).isEqualTo(user);
		assertThat(savedRefreshToken.getFamily()).isEqualTo(family);
		assertThat(savedRefreshToken.getCreationTimestamp().plus(refreshTokenProperties.getExpirationTime()))
				.isEqualTo(savedRefreshToken.getExpirationTimestamp());
	}

	@Test
	void createRefreshToken_shouldCreateAndSaveValidRefreshTokenWithNewFamily_whenAtLeastOneFamilyExists() {
		User user = user().withId().build();

		long lastTokenFamily = 5L;
		given(refreshTokenRepository.findLastTokenFamilyOfUser(user)).willReturn(Optional.of(lastTokenFamily));


		UUID createdRefreshTokenValue = refreshTokenService.createRefreshToken(user);


		verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
		RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

		assertThat(savedRefreshToken.getValue()).isEqualTo(createdRefreshTokenValue);
		assertThat(savedRefreshToken.getUser()).isEqualTo(user);
		assertThat(savedRefreshToken.getFamily()).isEqualTo(lastTokenFamily + 1L);
		assertThat(savedRefreshToken.getCreationTimestamp().plus(refreshTokenProperties.getExpirationTime()))
				.isEqualTo(savedRefreshToken.getExpirationTimestamp());
	}

	@Test
	void createRefreshToken_shouldCreateAndSaveValidRefreshTokenWithNewFirstFamily_whenNoFamilyExists() {
		User user = user().withId().build();


		given(refreshTokenRepository.findLastTokenFamilyOfUser(user)).willReturn(Optional.empty());


		UUID createdRefreshTokenValue = refreshTokenService.createRefreshToken(user);


		verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
		RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

		assertThat(savedRefreshToken.getValue()).isEqualTo(createdRefreshTokenValue);
		assertThat(savedRefreshToken.getUser()).isEqualTo(user);
		assertThat(savedRefreshToken.getFamily()).isEqualTo(1L);
		assertThat(savedRefreshToken.getCreationTimestamp().plus(refreshTokenProperties.getExpirationTime()))
				.isEqualTo(savedRefreshToken.getExpirationTimestamp());
	}


	@Test
	void findRefreshTokenByValue_shouldReturnPersistedRefreshToken_whenTokenWithSpecifiedValueExists() {
		User user = user().withId().build();

		UUID refreshTokenValue = UUID.randomUUID();
		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(refreshTokenValue)
				.family(10L)
				.user(user)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();

		given(refreshTokenRepository.findById(refreshTokenValue)).willReturn(Optional.of(persistedRefreshToken));


		Optional<RefreshToken> foundRefreshTokenOptional =
				refreshTokenService.findRefreshTokenByValue(refreshTokenValue);


		assertThat(foundRefreshTokenOptional).hasValue(persistedRefreshToken);
	}

	@Test
	void findRefreshTokenByValue_shouldReturnEmptyOptional_whenTokenWithSpecifiedValueDoesntExist() {
		UUID refreshTokenValue = UUID.randomUUID();

		given(refreshTokenRepository.findById(refreshTokenValue)).willReturn(Optional.empty());


		Optional<RefreshToken> foundRefreshTokenOptional =
				refreshTokenService.findRefreshTokenByValue(refreshTokenValue);


		assertThat(foundRefreshTokenOptional).isEmpty();
	}


	@Test
	void isRefreshTokenExpired_shouldReturnTrue_whenRefreshTokenHasExpired() {
		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(10L)
				.user(user().withId().build())
				.creationTimestamp(Instant.now().minus(2, ChronoUnit.HOURS))
				.expirationTimestamp(Instant.now().minus(1, ChronoUnit.HOURS))
				.build();


		boolean expired = refreshTokenService.isRefreshTokenExpired(refreshToken);


		assertThat(expired).isTrue();
	}

	@Test
	void isRefreshTokenExpired_shouldReturnFalse_whenRefreshTokenHasNotExpired() {
		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(10L)
				.user(user().withId().build())
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		boolean expired = refreshTokenService.isRefreshTokenExpired(refreshToken);


		assertThat(expired).isFalse();
	}


	@Test
	void findCurrentAllowedRefreshToken_shouldReturnLastRefreshTokenOfFamilyOptional_whenFamilyExists() {
		User user = user().withId().build();
		long family = 5L;

		RefreshToken persistedLastTokenOfFamily = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(10L)
				.user(user().withId().build())
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();

		given(refreshTokenRepository.findLastTokenOfUserTokenFamily(user, family))
				.willReturn(Optional.of(persistedLastTokenOfFamily));


		Optional<RefreshToken> currentAllowedTokenOptional =
				refreshTokenService.findCurrentAllowedRefreshToken(user, family);


		assertThat(currentAllowedTokenOptional).hasValue(persistedLastTokenOfFamily);
	}

	@Test
	void findCurrentAllowedRefreshToken_shouldReturnEmptyOptional_whenFamilyDoesntExist() {
		User user = user().withId().build();
		long family = 5L;

		given(refreshTokenRepository.findLastTokenOfUserTokenFamily(user, family))
				.willReturn(Optional.empty());


		Optional<RefreshToken> currentAllowedTokenOptional =
				refreshTokenService.findCurrentAllowedRefreshToken(user, family);


		assertThat(currentAllowedTokenOptional).isEmpty();
	}


	@Test
	void invalidateUserRefreshTokenFamily_shouldDeleteAllRefreshTokensOfUserTokenFamily() {
		User user = user().withId().build();
		long family = 5L;


		refreshTokenService.invalidateUserRefreshTokenFamily(user, family);


		verify(refreshTokenRepository).deleteAllTokensOfUserTokenFamily(user, family);
	}

}