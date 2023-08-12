package dyamo.narek.syntechnica.tokens.refresh;


import dyamo.narek.syntechnica.tokens.family.TokenFamily;
import dyamo.narek.syntechnica.users.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RefreshTokenRepositoryTests {

	@Autowired
	TestEntityManager testEntityManager;


	@Test
	void  tokenFamilyDeletionIsCascadedOnRefreshTokens() {
		User user = user().build();
		testEntityManager.persistAndFlush(user);

		var tokenFamily = TokenFamily.builder()
				.user(user)
				.lastGeneration(1L)
				.build();
		testEntityManager.persistAndFlush(tokenFamily);

		var refreshToken1 = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(tokenFamily)
				.generation(1L)
				.creationTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
				.expirationTimestamp(Instant.now().plus(30, ChronoUnit.MINUTES))
				.build();
		testEntityManager.persist(refreshToken1);

		var refreshToken2 = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(tokenFamily)
				.generation(2L)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();
		testEntityManager.persist(refreshToken2);


		testEntityManager.remove(user);
		testEntityManager.flush();
		testEntityManager.detach(refreshToken1);
		testEntityManager.detach(refreshToken2);


		var foundRefreshToken1 = testEntityManager.find(RefreshToken.class, refreshToken1.getValue());
		assertThat(foundRefreshToken1).isNull();

		var foundRefreshToken2 = testEntityManager.find(RefreshToken.class, refreshToken2.getValue());
		assertThat(foundRefreshToken2).isNull();
	}


}
