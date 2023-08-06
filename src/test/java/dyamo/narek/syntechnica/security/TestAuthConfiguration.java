package dyamo.narek.syntechnica.security;

import dyamo.narek.syntechnica.tokens.access.AccessTokenVersionProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@TestConfiguration
@EnableMethodSecurity
public class TestAuthConfiguration {

	@Bean
	public AccessTokenVersionProvider accessTokenVersionProvider() {
		return username -> 1L;
	}

}
