package dyamo.narek.syntechnica.tokens.access;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.auth.tokens.access")
@Data
public class AccessTokenConfigurationProperties {

	private String issuer;

	private Duration expirationTime;


	private Claims claims;


	@Data
	public static class Claims {

		private String authorities;

		private String version;

		private String family;

		private String generation;

	}

}
