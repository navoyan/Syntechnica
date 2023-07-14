package dyamo.narek.syntechnica.users;

import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityBuilder;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityType;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static dyamo.narek.syntechnica.users.UserBuilder.user;
import static dyamo.narek.syntechnica.users.authorities.UserAuthorityBuilder.authority;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

@DataJpaTest
class UserRepositoryTests {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TestEntityManager testEntityManager;


	@BeforeEach
	void beforeEach() {
		UserBuilder.resetIndex();
		UserAuthorityBuilder.resetIndex();
	}


	@Test
	void countByAuthorityId_shouldReturnCountOfUsersWithGivenAuthority() {
		UserAuthority adminAuthority = testEntityManager.persist(authority().withType(UserAuthorityType.ADMIN).build());

		testEntityManager.persist(user().withAuthorities(adminAuthority).build());
		testEntityManager.persist(user().withAuthorities(adminAuthority).build());
		testEntityManager.persist(user().build());


		int count = userRepository.countByAuthorityId(adminAuthority.getId());


		assertThat(count).isEqualTo(2);
	}

	@Test
	void usernamesAreUnique() {
		User user1 = user().withName("user").build();
		User user2 = user().withName("user").build();


		testEntityManager.persistAndFlush(user1);
		Exception thrown = catchException(() -> {
			testEntityManager.persistAndFlush(user2);
		});


		assertThat(thrown).isInstanceOfAny(ConstraintViolationException.class, PersistenceException.class);
	}

}