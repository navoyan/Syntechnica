package dyamo.narek.syntechnica.tokens.family;


import dyamo.narek.syntechnica.users.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TokenFamilyRepositoryTests {

	@Autowired
	TestEntityManager testEntityManager;


	@Test
	void userDeletionIsCascadedOnTokenFamilies() {
		User user = user().build();
		testEntityManager.persistAndFlush(user);

		var tokenFamily1 = TokenFamily.builder()
				.user(user)
				.lastGeneration(1L)
				.build();
		testEntityManager.persistAndFlush(tokenFamily1);

		var tokenFamily2 = TokenFamily.builder()
				.user(user)
				.lastGeneration(1L)
				.build();
		testEntityManager.persistAndFlush(tokenFamily2);


		testEntityManager.remove(user);
		testEntityManager.flush();
		testEntityManager.detach(tokenFamily1);
		testEntityManager.detach(tokenFamily2);


		var foundTokenFamily1 = testEntityManager.find(TokenFamily.class, tokenFamily1.getId());
		assertThat(foundTokenFamily1).isNull();

		var foundTokenFamily2 = testEntityManager.find(TokenFamily.class, tokenFamily2.getId());
		assertThat(foundTokenFamily2).isNull();
	}


}
