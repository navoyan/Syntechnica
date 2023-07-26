package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.security.auth.tokens.TokenService;
import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final TokenService tokenService;


	public @NonNull TokenResponse generateTokens(@NonNull @Valid UserCredentials credentials) {
		String username = credentials.getUsername();
		User user = userService.findUserByName(username).orElseThrow(() ->
				new UsernameNotFoundException(String.format("User with username %s is not found", username))
		);

		if (!userService.isUserMatchingPassword(user, credentials.getPassword())) {
			throw new BadCredentialsException("Invalid password");
		}

		return new TokenResponse(tokenService.createAccessToken(user));
	}

}
