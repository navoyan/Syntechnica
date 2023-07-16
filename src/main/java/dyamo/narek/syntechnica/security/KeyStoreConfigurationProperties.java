package dyamo.narek.syntechnica.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.security.keystore")
@Data
public class KeyStoreConfigurationProperties {

	private String issuer;

	private Path path;

	private String password;


	private JwtSigningKey jwtSigningKey;


	@Data
	public static class JwtSigningKey {

		private String alias;

		private String password;

	}

}
