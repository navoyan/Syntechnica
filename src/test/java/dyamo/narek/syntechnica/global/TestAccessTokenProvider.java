package dyamo.narek.syntechnica.global;

import dyamo.narek.syntechnica.security.auth.tokens.access.AccessTokenConfigurationProperties;
import dyamo.narek.syntechnica.users.User;
import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestAccessTokenProvider {

	private final AccessTokenConfigurationProperties accessTokenProperties;

	private final JwtEncoder jwtEncoder;


	public TestAccessTokenProvider(AccessTokenConfigurationProperties accessTokenConfigurationProperties, JwtEncoder jwtEncoder) {
		this.accessTokenProperties = accessTokenConfigurationProperties;
		this.jwtEncoder = jwtEncoder;
	}


	public @NonNull RequestPostProcessor bearerAccessToken(@NonNull @Valid User user) {
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
				.claim(accessTokenProperties.getClaims().getVersion(), 1L)
				.build();


		String encodedToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();


		return request -> {
			request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + encodedToken);
			return request;
		};
	}

}
