package dyamo.narek.syntechnica.security.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.auth.jwt")
@Data
public class JwtConfigurationProperties {

	private String issuer;

	private Duration expirationTime;


	private Claims claims;


	@Data
	public static class Claims {

		private String authorities;

	}

}
