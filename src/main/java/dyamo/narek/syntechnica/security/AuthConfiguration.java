package dyamo.narek.syntechnica.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dyamo.narek.syntechnica.tokens.access.AccessTokenConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class AuthConfiguration {

	@Bean
	public AuthenticationManager authenticationManager(ValidFamilyJwtAuthenticationProvider firstProviderInChain) {
		return new ProviderManager(firstProviderInChain);
	}

	@Bean
	public JwtAuthenticationProvider jwtAuthenticationProvider(JwtDecoder jwtDecoder) {
		return new JwtAuthenticationProvider(jwtDecoder);
	}

	
	@Bean
	public JwtEncoder jwtEncoder(RSAPublicKey jwtValidationKey, RSAPrivateKey jwtSigningKey) {
		JWK jwk = new RSAKey.Builder(jwtValidationKey).privateKey(jwtSigningKey).build();
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	public JwtDecoder jwtDecoder(RSAPublicKey jwtValidationKey) {
		return NimbusJwtDecoder.withPublicKey(jwtValidationKey).build();
	}


	@Bean
	public JwtAuthenticationConverter authenticationConverter(AccessTokenConfigurationProperties accessTokenProperties) {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthorityPrefix("");
		authoritiesConverter.setAuthoritiesClaimName(accessTokenProperties.getClaims().getAuthorities());

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

		return converter;
	}

}
