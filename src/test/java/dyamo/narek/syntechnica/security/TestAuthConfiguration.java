package dyamo.narek.syntechnica.security;

import dyamo.narek.syntechnica.tokens.access.AccessTokenVersionProvider;
import dyamo.narek.syntechnica.tokens.family.TokenGenerationValidityCoordinator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Optional;

@TestConfiguration
@EnableMethodSecurity
public class TestAuthConfiguration {

	@Bean
	public AccessTokenVersionProvider accessTokenVersionProvider() {
		return username -> 1L;
	}

	@Bean
	public TokenGenerationValidityCoordinator tokenGenerationValidityCoordinator() {
		return new TokenGenerationValidityCoordinator() {
			@Override
			public Optional<Long> getTokenFamilyLastGeneration(long familyId) {
				return Optional.of(1L);
			}

			@Override
			public void invalidateTokenFamily(long familyId) {
			}
		};
	}

}
