package dyamo.narek.syntechnica.tokens;

import dyamo.narek.syntechnica.tokens.access.AccessTokenService;
import dyamo.narek.syntechnica.tokens.family.TokenFamily;
import dyamo.narek.syntechnica.tokens.family.TokenFamilyService;
import dyamo.narek.syntechnica.tokens.refresh.InvalidRefreshTokenException;
import dyamo.narek.syntechnica.tokens.refresh.ProhibitedRefreshTokenException;
import dyamo.narek.syntechnica.tokens.refresh.RefreshToken;
import dyamo.narek.syntechnica.tokens.refresh.RefreshTokenService;
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
public class TokenPairService {

	private final UserService userService;

	private final TokenFamilyService tokenFamilyService;

	private final AccessTokenService accessTokenService;

	private final RefreshTokenService refreshTokenService;


	@Transactional
	public @NonNull TokenPairResponse generateTokens(@NonNull @Valid UserCredentialsRequest credentials) {
		String username = credentials.getUsername();
		User user = userService.findUserByName(username).orElseThrow(() ->
				new InvalidCredentialsException(String.format("User with username %s is not found", username))
		);

		if (!userService.isUserMatchingPassword(user, credentials.getPassword())) {
			throw new InvalidCredentialsException("Invalid password");
		}


		TokenFamily newFamily = tokenFamilyService.createTokenFamily(user);

		return new TokenPairResponse(
				accessTokenService.createAccessToken(newFamily),
				refreshTokenService.createRefreshToken(newFamily)
		);
	}

	@Transactional(noRollbackFor = ProhibitedRefreshTokenException.class)
	public @NonNull TokenPairResponse generateTokens(@NonNull UUID refreshTokenValue) {
		RefreshToken providedRefreshToken = refreshTokenService.findRefreshTokenByValue(refreshTokenValue).orElseThrow(
				() -> new InvalidRefreshTokenException("Refresh token not found, possibly invalidated or expired")
		);

		TokenFamily family = providedRefreshToken.getFamily();

		if (!providedRefreshToken.isLastGeneration()) {
			tokenFamilyService.invalidateTokenFamily(family);
			throw new ProhibitedRefreshTokenException("Prohibited refresh token, already used");
		}

		if (providedRefreshToken.isExpired()) {
			throw new InvalidRefreshTokenException(
					"Refresh token expired at " + providedRefreshToken.getExpirationTimestamp()
			);
		}


		tokenFamilyService.updateTokenFamilyLastGeneration(family);

		return new TokenPairResponse(
				accessTokenService.createAccessToken(family),
				refreshTokenService.createRefreshToken(family)
		);
	}

}
