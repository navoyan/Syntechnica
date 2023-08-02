package dyamo.narek.syntechnica.security.auth.tokens.access;

import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccessTokenMetadataRepositoryTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	AccessTokenMetadataRepository accessTokenMetadataRepository;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void findByUsername_shouldReturnPresentTokenMetadataOptional_whenUserExists() {
		User searchedMetadataUser = user().build();
		testEntityManager.persist(searchedMetadataUser);

		User user = user().build();
		testEntityManager.persist(user);

		var searchedTokenMetadata = AccessTokenMetadata.builder()
				.userId(searchedMetadataUser.getId()).user(searchedMetadataUser).version(5)
				.build();
		searchedMetadataUser.setAccessTokenMetadata(searchedTokenMetadata);
		testEntityManager.persist(searchedTokenMetadata);

		var tokenMetadata = AccessTokenMetadata.builder()
				.userId(user.getId()).user(user).version(4)
				.build();
		user.setAccessTokenMetadata(tokenMetadata);
		testEntityManager.persist(tokenMetadata);


		var foundMetadataOptional = accessTokenMetadataRepository.findByUsername(searchedMetadataUser.getName());


		assertThat(foundMetadataOptional).hasValue(searchedTokenMetadata);
	}

	@Test
	void findByUsername_shouldReturnEmptyOptional_whenUserDoesntExist() {
		String notExistingUsername = "not_existing";

		User user = user().build();
		testEntityManager.persist(user);

		var tokenMetadata = AccessTokenMetadata.builder()
				.userId(user.getId()).user(user).version(4)
				.build();
		user.setAccessTokenMetadata(tokenMetadata);
		testEntityManager.persist(tokenMetadata);


		var foundMetadataOptional = accessTokenMetadataRepository.findByUsername(notExistingUsername);


		assertThat(foundMetadataOptional).isEmpty();
	}


	@Test
	void userDeletionIsCascadedOnTokenMetadata() {
		User user = user().build();
		testEntityManager.persistAndFlush(user);

		var tokenMetadata = AccessTokenMetadata.builder()
				.userId(user.getId()).user(user).version(4)
				.build();
		user.setAccessTokenMetadata(tokenMetadata);
		testEntityManager.persistAndFlush(tokenMetadata);


		testEntityManager.remove(user);
		testEntityManager.flush();
		testEntityManager.detach(tokenMetadata);


		var foundTokenMetadata = testEntityManager.find(AccessTokenMetadata.class, tokenMetadata.getUserId());
		assertThat(foundTokenMetadata).isNull();
	}

}