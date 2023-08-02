package dyamo.narek.syntechnica.security.auth.tokens.refresh;

import dyamo.narek.syntechnica.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	private final RefreshTokenConfigurationProperties refreshTokenConfigurationProperties;


	public @NonNull UUID createRefreshToken(@NonNull @Valid User user, long family) {
		UUID refreshTokenValue = UUID.randomUUID();

		Instant now = Instant.now();
		RefreshToken refreshToken = RefreshToken.builder()
				.value(refreshTokenValue)
				.user(user)
				.family(family)
				.creationTimestamp(now)
				.expirationTimestamp(now.plus(refreshTokenConfigurationProperties.getExpirationTime()))
				.build();

		refreshTokenRepository.save(refreshToken);

		return refreshTokenValue;
	}

	public @NonNull UUID createRefreshToken(@NonNull @Valid User user) {
		long lastFamily = refreshTokenRepository.findLastTokenFamilyOfUser(user).orElse(0L);

		return createRefreshToken(user, lastFamily + 1);
	}


	public Optional<RefreshToken> findRefreshTokenByValue(@NonNull UUID refreshTokenValue) {
		return refreshTokenRepository.findById(refreshTokenValue);
	}

	public boolean isRefreshTokenExpired(@NonNull @Valid RefreshToken refreshToken) {
		return refreshToken.getExpirationTimestamp().isBefore(Instant.now());
	}


	public Optional<RefreshToken> findCurrentAllowedRefreshToken(@NonNull User user, long family) {
		return refreshTokenRepository.findLastTokenOfUserTokenFamily(user, family);
	}

	public void invalidateUserRefreshTokenFamily(@NonNull User user, long family) {
		refreshTokenRepository.deleteAllTokensOfUserTokenFamily(user, family);
	}

}
