package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.security.auth.tokens.access.AccessTokenService;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.InvalidRefreshTokenException;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.ProhibitedRefreshTokenException;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.RefreshToken;
import dyamo.narek.syntechnica.security.auth.tokens.refresh.RefreshTokenService;
import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;

	private final AccessTokenService accessTokenService;

	private final RefreshTokenService refreshTokenService;


	@Transactional
	public @NonNull TokenResponse generateTokens(@NonNull @Valid UserCredentials credentials) {
		String username = credentials.getUsername();
		User user = userService.findUserByName(username).orElseThrow(() ->
				new InvalidCredentialsException(String.format("User with username %s is not found", username))
		);

		if (!userService.isUserMatchingPassword(user, credentials.getPassword())) {
			throw new InvalidCredentialsException("Invalid password");
		}

		return new TokenResponse(
				accessTokenService.createAccessToken(user),
				refreshTokenService.createRefreshToken(user)
		);
	}

	@Transactional(noRollbackFor = ProhibitedRefreshTokenException.class)
	public @NonNull TokenResponse generateTokens(@NonNull UUID refreshTokenValue) {
		RefreshToken providedRefreshToken = refreshTokenService.findRefreshTokenByValue(refreshTokenValue).orElseThrow(
				() -> new InvalidRefreshTokenException("Refresh token not found")
		);

		User user = providedRefreshToken.getUser();
		long family = providedRefreshToken.getFamily();

		var currentAllowedRefreshToken = refreshTokenService.findCurrentAllowedRefreshToken(user, family).orElse(null);
		if (!providedRefreshToken.equals(currentAllowedRefreshToken)) {
			refreshTokenService.invalidateUserRefreshTokenFamily(user, family);
			throw new ProhibitedRefreshTokenException("Prohibited refresh token");
		}

		if (refreshTokenService.isRefreshTokenExpired(providedRefreshToken)) {
			throw new InvalidRefreshTokenException(
					"Refresh token expired at " + providedRefreshToken.getExpirationTimestamp()
			);
		}


		return new TokenResponse(
				accessTokenService.createAccessToken(user),
				refreshTokenService.createRefreshToken(user, family)
		);
	}

}
