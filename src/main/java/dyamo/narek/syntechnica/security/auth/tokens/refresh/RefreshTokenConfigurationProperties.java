package dyamo.narek.syntechnica.security.auth.tokens.refresh;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.auth.tokens.refresh")
@Data
public class RefreshTokenConfigurationProperties {

	private Duration expirationTime;

}
