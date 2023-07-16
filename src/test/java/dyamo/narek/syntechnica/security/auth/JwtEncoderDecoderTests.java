package dyamo.narek.syntechnica.security.auth;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import dyamo.narek.syntechnica.security.KeyStoreConfiguration;
import dyamo.narek.syntechnica.security.KeyStoreConfigurationProperties;
import dyamo.narek.syntechnica.security.KeyStoreProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {
				AuthConfiguration.class, KeyStoreConfiguration.class,
				KeyStoreConfigurationProperties.class, KeyStoreProvider.class
		})
@ContextConfiguration(initializers = JwtEncoderDecoderTests.ContextInitializer.class)
@EnableConfigurationProperties
class JwtEncoderDecoderTests {

	@Autowired
	KeyStoreProvider keyStoreProvider;

	@Autowired
	JwtEncoder jwtEncoder;
	@Autowired
	JwtDecoder jwtDecoder;


	@TempDir
	static Path tempParentDirectory;


	@Test
	void shouldSuccessfullyDecodeAndValidate_whenJwtIsSignedBy() {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("syntechnica")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject("user")
				.claim("authorities", List.of("ADMIN", "READ:*", "WRITE:*"))
				.build();

		String encodedJwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		Jwt decodedJwt = jwtDecoder.decode(encodedJwt);

		assertThat(decodedJwt.getTokenValue()).isEqualTo(encodedJwt);
	}

	@Test
	void shouldThrowException_whenJwtIsNotSigned() {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.issuer("syntechnica")
				.issueTime(Date.from(Instant.now()))
				.expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
				.subject("user")
				.claim("authorities", List.of("ADMIN", "READ:*", "WRITE:*"))
				.build();

		String encodedJwt = new PlainJWT(claims).serialize();


		Exception thrown = catchException(() -> {
			jwtDecoder.decode(encodedJwt);
		});


		assertThat(thrown).isNotNull();
	}

	@Test
	void shouldThrowException_whenJwtIsSignedByForeignKey() {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("syntechnica")
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.subject("user")
				.claim("authorities", List.of("ADMIN", "READ:*", "WRITE:*"))
				.build();

		KeyPair foreignKeyPair = keyStoreProvider.generateRSAKeyPair(new SecureRandom());

		JWK jwk = new RSAKey.Builder((RSAPublicKey) foreignKeyPair.getPublic())
				.privateKey((RSAPrivateKey) foreignKeyPair.getPrivate())
				.build();
		JwtEncoder notSignedJwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));

		String encodedJwt = notSignedJwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();


		Exception thrown = catchException(() -> {
			jwtDecoder.decode(encodedJwt);
		});


		assertThat(thrown).isNotNull();
	}



	static class ContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
			TestPropertyValues testPropertyValues = TestPropertyValues.of(
					"app.security.keystore.path=" + tempParentDirectory.resolve("keystore.p12")
			);
			testPropertyValues.applyTo(applicationContext);
		}
	}

}