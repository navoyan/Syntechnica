package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.security.auth.tokens.AccessTokenVersionProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestAuthConfiguration {

	@Bean
	public AccessTokenVersionProvider accessTokenVersionProvider() {
		return username -> 1L;
	}

}
