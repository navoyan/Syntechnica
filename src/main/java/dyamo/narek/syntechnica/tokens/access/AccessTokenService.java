package dyamo.narek.syntechnica.tokens.access;

import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessTokenService implements AccessTokenVersionProvider {

	private final AccessTokenConfigurationProperties accessTokenProperties;

	private final JwtEncoder jwtEncoder;

	private final AccessTokenMetadataRepository accessTokenMetadataRepository;


	public @NonNull String createAccessToken(@NonNull @Valid User user) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(accessTokenProperties.getExpirationTime());

		List<String> authorities = user.getAuthorities().stream()
				.map(UserAuthority::getAuthority)
				.collect(Collectors.toList());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(accessTokenProperties.getIssuer())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(user.getName())
				.claim(accessTokenProperties.getClaims().getAuthorities(), authorities)
				.claim(accessTokenProperties.getClaims().getVersion(), getAccessTokenCurrentVersion(user))
				.build();


		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	@Cacheable(cacheNames = "versions", cacheManager = "accessTokenMetadataCacheManager")
	@Override
	public long getAccessTokenCurrentVersion(@NonNull String username) {
		return accessTokenMetadataRepository.findByUsername(username)
				.map(AccessTokenMetadata::getVersion)
				.orElse(1L);
	}


	private long getAccessTokenCurrentVersion(@NonNull User user) {
		return accessTokenMetadataRepository.findById(user.getId())
				.map(AccessTokenMetadata::getVersion)
				.orElse(1L);
	}

}
