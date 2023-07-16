package dyamo.narek.syntechnica.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KeyStoreConfiguration {

	private final KeyStoreProvider keyStoreProvider;

	private final KeyStoreConfigurationProperties properties;


	@Bean
	@SneakyThrows
	public KeyStore keyStore() {
		return keyStoreProvider.loadOrCreateKeyStore();
	}

	@Bean
	@SneakyThrows
	public RSAPrivateKey jwtSigningKey(KeyStore keyStore) {
		String alias = properties.getJwtSigningKey().getAlias();
		char[] password = properties.getJwtSigningKey().getPassword().toCharArray();

		return (RSAPrivateKey) keyStore.getKey(alias, password);
	}

	@Bean
	@SneakyThrows
	public RSAPublicKey jwtValidationKey(KeyStore keyStore) {
		String alias = properties.getJwtSigningKey().getAlias();

		return (RSAPublicKey) keyStore.getCertificate(alias).getPublicKey();
	}

}
