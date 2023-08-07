package dyamo.narek.syntechnica.tokens.refresh;

import dyamo.narek.syntechnica.tokens.family.TokenFamily;
import dyamo.narek.syntechnica.users.TestUserBuilder;
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
		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.build();


		UUID createdRefreshTokenValue = refreshTokenService.createRefreshToken(tokenFamily);


		verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
		RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

		assertThat(savedRefreshToken.getValue()).isEqualTo(createdRefreshTokenValue);
		assertThat(savedRefreshToken.getFamily()).isEqualTo(tokenFamily);
		assertThat(savedRefreshToken.getCreationTimestamp().plus(refreshTokenProperties.getExpirationTime()))
				.isEqualTo(savedRefreshToken.getExpirationTimestamp());
	}


	@Test
	void findRefreshTokenByValue_shouldReturnPersistedRefreshToken_whenTokenWithSpecifiedValueExists() {
		UUID refreshTokenValue = UUID.randomUUID();
		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.build();

		RefreshToken persistedRefreshToken = RefreshToken.builder()
				.value(refreshTokenValue)
				.family(tokenFamily)
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

}