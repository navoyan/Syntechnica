package dyamo.narek.syntechnica.users;

import dyamo.narek.syntechnica.users.authorities.TestUserAuthorityBuilder;
import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityService;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static dyamo.narek.syntechnica.users.TestUserBuilder.VALID_ENCODED_PASSWORD;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static dyamo.narek.syntechnica.users.authorities.TestUserAuthorityBuilder.authority;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

	@Mock
	UserRepository userRepository;
	@Mock
	UserAuthorityService userAuthorityService;
	@Mock
	PasswordEncoder passwordEncoder;

	@InjectMocks
	UserService userService;


	@Captor
	ArgumentCaptor<User> userCaptor;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
		TestUserAuthorityBuilder.resetIndex();
	}


	@Test
	void findUserByName_shouldReturnPresentUserOptional_whenUserExists() {
		User existingUser = user().withId().build();

		given(userRepository.findByName(existingUser.getName())).willReturn(Optional.of(existingUser));


		Optional<User> foundUserOptional = userService.findUserByName(existingUser.getName());


		assertThat(foundUserOptional).hasValue(existingUser);
	}

	@Test
	void findUserByName_shouldReturnEmptyOptional_whenUserNotExists() {
		given(userRepository.findByName(anyString())).willReturn(Optional.empty());


		Optional<User> notExistingUserOptional = userService.findUserByName("not existing");


		assertThat(notExistingUserOptional).isEmpty();
	}


	@Test
	void isUserMatchingPassword_shouldReturnTrue_whenPasswordMatches() {
		User existingUser = user().withId().build();
		String rawActualPassword = "actual password";

		given(passwordEncoder.matches(rawActualPassword, existingUser.getPassword())).willReturn(true);


		boolean matching = userService.isUserMatchingPassword(existingUser, rawActualPassword);


		assertThat(matching).isTrue();
	}

	@Test
	void isUserMatchingPassword_shouldReturnFalse_whenPasswordDoesNotMatch() {
		User existingUser = user().withId().build();
		String invalidPassword = "invalid password";

		given(passwordEncoder.matches(invalidPassword, existingUser.getPassword())).willReturn(false);


		boolean matching = userService.isUserMatchingPassword(existingUser, invalidPassword);


		assertThat(matching).isFalse();
	}


	@Test
	void saveDefaultAdminUserIfNotExists_shouldSaveUser_whenNoOtherAdmin() {
		UserAuthority adminAuthority = authority().withId().withType(UserAuthorityType.ADMIN).build();

		given(userAuthorityService.findByType(UserAuthorityType.ADMIN)).willReturn(List.of(adminAuthority));
		given(userRepository.countByAuthorityId(adminAuthority.getId())).willReturn(0);
		given(passwordEncoder.encode(anyString())).willReturn(VALID_ENCODED_PASSWORD);


		userService.saveDefaultAdminUserIfNotExists();


		verify(userRepository).save(userCaptor.capture());
		User savedAdmin = userCaptor.getValue();

		assertThat(savedAdmin.getAuthorities()).containsOnly(adminAuthority);
		assertThat(savedAdmin.getPassword()).isEqualTo(VALID_ENCODED_PASSWORD);
		assertThat(adminAuthority.getUsers()).containsOnly(savedAdmin);
	}

	@Test
	void saveDefaultAdminUserIfNotExists_shouldDoNothing_whenThereAreOtherAdmins() {
		UserAuthority adminAuthority = authority().withId().withType(UserAuthorityType.ADMIN).build();
		user().withId().withAuthorities(adminAuthority).build();

		given(userAuthorityService.findByType(UserAuthorityType.ADMIN)).willReturn(List.of(adminAuthority));
		given(userRepository.countByAuthorityId(adminAuthority.getId())).willReturn(1);


		userService.saveDefaultAdminUserIfNotExists();


		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void saveDefaultAdminUserIfNotExists_shouldThrowException_whenAdminAuthorityNotFound() {
		given(userAuthorityService.findByType(UserAuthorityType.ADMIN)).willReturn(Collections.emptyList());


		Exception thrown = catchException(() -> {
			userService.saveDefaultAdminUserIfNotExists();
		});


		assertThat(thrown).isNotNull();
	}

}