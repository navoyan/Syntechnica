package dyamo.narek.syntechnica.security.auth.tokens.refresh;

import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefreshTokenRepositoryTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	RefreshTokenRepository refreshTokenRepository;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void findLastTokenFamilyOfUser_shouldReturnLastTokenFamilyOptional_whenUserHasAnyRefreshToken() {
		User notRelatedUser = user().build();
		testEntityManager.persist(notRelatedUser);

		RefreshToken notRelatedRefreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(10L)
				.user(notRelatedUser)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();
		testEntityManager.persist(notRelatedRefreshToken);

		User user = user().build();
		testEntityManager.persist(user);

		List<Long> families = List.of(5L, 1L, 4L, 4L, 5L, 4L, 2L, 2L, 3L, 3L);
		for (long family : families) {
			RefreshToken refreshToken = RefreshToken.builder()
					.value(UUID.randomUUID())
					.family(family)
					.user(user)
					.creationTimestamp(Instant.now())
					.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
					.build();
			testEntityManager.persist(refreshToken);
		}


		Optional<Long> lastFamilyOptional = refreshTokenRepository.findLastTokenFamilyOfUser(user);


		assertThat(lastFamilyOptional).isEqualTo(families.stream().max(Comparator.naturalOrder()));
	}

	@Test
	void findLastTokenFamilyOfUser_shouldReturnEmptyOptional_whenUserHasNoRefreshTokens() {
		User notRelatedUser = user().build();
		testEntityManager.persist(notRelatedUser);

		RefreshToken notRelatedRefreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(10L)
				.user(notRelatedUser)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();
		testEntityManager.persist(notRelatedRefreshToken);

		User user = user().build();
		testEntityManager.persist(user);


		Optional<Long> lastFamilyOptional = refreshTokenRepository.findLastTokenFamilyOfUser(user);


		assertThat(lastFamilyOptional).isEmpty();
	}


	@Test
	void findLastTokenOfUserTokenFamily_shouldReturnLastTokenOfFamilyOptional_whenUserAndFamilyExist() {
		User notRelatedUser = user().build();
		testEntityManager.persist(notRelatedUser);

		RefreshToken notRelatedRefreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(1L)
				.user(notRelatedUser)
				.creationTimestamp(Instant.now().plus(10, ChronoUnit.HOURS))
				.expirationTimestamp(Instant.now().plus(11, ChronoUnit.HOURS))
				.build();
		testEntityManager.persist(notRelatedRefreshToken);

		User user = user().build();
		testEntityManager.persist(user);

		RefreshToken lastCreatedRefreshToken = null;
		for (int family = 1; family <= 2; family++) {
			for (int creationTimeOffset = 1; creationTimeOffset <= 3; creationTimeOffset++) {
				lastCreatedRefreshToken = RefreshToken.builder()
						.value(UUID.randomUUID())
						.family(family)
						.user(user)
						.creationTimestamp(Instant.now().plus(creationTimeOffset, ChronoUnit.HOURS))
						.expirationTimestamp(Instant.now().plus(creationTimeOffset + 1, ChronoUnit.HOURS))
						.build();
				testEntityManager.persist(lastCreatedRefreshToken);
			}
		}


		Optional<RefreshToken> lastTokenOfUserFamilyOptional =
				refreshTokenRepository.findLastTokenOfUserTokenFamily(user, 2L);


		assertThat(lastTokenOfUserFamilyOptional).hasValue(lastCreatedRefreshToken);
	}

	@Test
	void findLastTokenOfUserTokenFamily_shouldReturnEmptyOptional_whenUserHasNoRefreshTokens() {
		User notRelatedUser = user().build();
		testEntityManager.persist(notRelatedUser);

		RefreshToken notRelatedRefreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(1L)
				.user(notRelatedUser)
				.creationTimestamp(Instant.now().plus(10, ChronoUnit.HOURS))
				.expirationTimestamp(Instant.now().plus(11, ChronoUnit.HOURS))
 				.build();
		testEntityManager.persist(notRelatedRefreshToken);

		User user = user().build();
		testEntityManager.persist(user);


		Optional<RefreshToken> lastTokenOfUserFamilyOptional =
				refreshTokenRepository.findLastTokenOfUserTokenFamily(user, 1L);


		assertThat(lastTokenOfUserFamilyOptional).isEmpty();
	}


	@Test
	void deleteAllTokensOfUserTokenFamily() {
		List<RefreshToken> persistedRefreshTokens = new ArrayList<>();

		User notRelatedUser = user().build();
		testEntityManager.persist(notRelatedUser);

		RefreshToken notRelatedRefreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(1L)
				.user(notRelatedUser)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();
		persistedRefreshTokens.add(notRelatedRefreshToken);
		testEntityManager.persist(notRelatedRefreshToken);

		User user = user().build();
		testEntityManager.persist(user);

		for (long family = 1; family <= 2; family++) {
			for (int i = 0; i < 5; i++) {
				RefreshToken refreshToken = RefreshToken.builder()
						.value(UUID.randomUUID())
						.family(family)
						.user(user)
						.creationTimestamp(Instant.now())
						.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
						.build();

				persistedRefreshTokens.add(refreshToken);
				testEntityManager.persist(refreshToken);
			}
		}


		refreshTokenRepository.deleteAllTokensOfUserTokenFamily(user, 1L);
		testEntityManager.clear();


		assertThat(persistedRefreshTokens.stream().filter(token -> token.getUser() == user && token.getFamily() == 1L))
				.allSatisfy(token -> {
					assertThat(testEntityManager.find(RefreshToken.class, token.getValue())).isNull();
				});

		assertThat(persistedRefreshTokens.stream().filter(token -> token.getUser() != user || token.getFamily() != 1L))
				.allSatisfy(token -> {
					assertThat(testEntityManager.find(RefreshToken.class, token.getValue())).isNotNull();
				});
	}

}