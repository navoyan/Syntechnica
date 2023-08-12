package dyamo.narek.syntechnica.tokens.refresh;

import dyamo.narek.syntechnica.tokens.family.TokenFamily;
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


	public @NonNull UUID createRefreshToken(TokenFamily family) {
		UUID refreshTokenValue = UUID.randomUUID();

		Instant now = Instant.now();
		RefreshToken refreshToken = RefreshToken.builder()
				.value(refreshTokenValue)
				.family(family)
				.generation(family.getLastGeneration())
				.creationTimestamp(now)
				.expirationTimestamp(now.plus(refreshTokenConfigurationProperties.getExpirationTime()))
				.build();

		refreshTokenRepository.save(refreshToken);

		return refreshTokenValue;
	}


	public Optional<RefreshToken> findRefreshTokenByValue(@NonNull UUID refreshTokenValue) {
		return refreshTokenRepository.findById(refreshTokenValue);
	}

}
