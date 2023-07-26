package dyamo.narek.syntechnica.security.auth.tokens;

import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class TokenService {

	private final JwtConfigurationProperties properties;

	private final JwtEncoder jwtEncoder;


	public @NonNull String createAccessToken(@NonNull @Valid User user) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(properties.getExpirationTime());

		List<String> authorities = user.getAuthorities().stream()
				.map(UserAuthority::getAuthority)
				.collect(Collectors.toList());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.getIssuer())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(user.getName())
				.claim(properties.getClaims().getAuthorities(), authorities)
				.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

}
