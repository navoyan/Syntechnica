package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.security.auth.tokens.TokenService;
import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

	@Mock
	private UserService userService;
	@Mock
	private TokenService tokenService;

	@InjectMocks
	private AuthService authService;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void generateTokens_shouldGenerateValidTokenPair_whenProvidedCredentialsAreValid() {
		User user = user().build();
		var credentials = new UserCredentials(user.getName(), "password");

		given(userService.findUserByName(user.getName())).willReturn(Optional.of(user));
		given(userService.isUserMatchingPassword(user, credentials.getPassword())).willReturn(true);
		String encodedAccessToken = "ENCODED ACCESS TOKEN";
		given(tokenService.createAccessToken(user)).willReturn(encodedAccessToken);


		TokenResponse tokenResponse = authService.generateTokens(credentials);


		verify(tokenService).createAccessToken(user);

		assertThat(tokenResponse.getAccessToken()).isEqualTo(encodedAccessToken);
	}

	@Test
	void generateTokens_shouldThrowException_whenUserNotExists() {
		var credentials = new UserCredentials("user", "password");

		given(userService.findUserByName(credentials.getUsername())).willReturn(Optional.empty());


		Exception thrown = catchException(() -> {
			authService.generateTokens(credentials);
		});


		assertThat(thrown).isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void generateTokens_shouldThrowException_whenUserPasswordDoesNotMatch() {
		User user = user().build();
		var credentials = new UserCredentials(user.getName(), "password");

		given(userService.findUserByName(user.getName())).willReturn(Optional.of(user));
		given(userService.isUserMatchingPassword(user, credentials.getPassword())).willReturn(false);


		Exception thrown = catchException(() -> {
			authService.generateTokens(credentials);
		});


		assertThat(thrown).isInstanceOf(BadCredentialsException.class);
	}

}