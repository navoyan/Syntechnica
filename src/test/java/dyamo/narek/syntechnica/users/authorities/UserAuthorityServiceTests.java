package dyamo.narek.syntechnica.users.authorities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static dyamo.narek.syntechnica.users.authorities.TestUserAuthorityBuilder.authority;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class UserAuthorityServiceTests {

	@Mock
	private UserAuthorityRepository userAuthorityRepository;

	@InjectMocks
	private UserAuthorityService userAuthorityService;


	@Test
	void findByType_shouldReturnPersistedAuthoritiesOfGivenType() {
		List<UserAuthority> savedAuthorities = List.of(
				authority().withId().withType(UserAuthorityType.READ).withScope("*").build(),
				authority().withId().withType(UserAuthorityType.READ).withScope("docs/tables").build()
		);

		given(userAuthorityRepository.findByType(UserAuthorityType.READ)).willReturn(savedAuthorities);


		List<UserAuthority> foundAuthorities = userAuthorityService.findByType(UserAuthorityType.READ);


		assertThat(foundAuthorities).containsExactlyInAnyOrderElementsOf(savedAuthorities);
	}
}