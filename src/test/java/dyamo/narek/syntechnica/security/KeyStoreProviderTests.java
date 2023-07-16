package dyamo.narek.syntechnica.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {KeyStoreProvider.class, KeyStoreConfigurationProperties.class})
@EnableConfigurationProperties
class KeyStoreProviderTests {

	@Autowired
	KeyStoreConfigurationProperties keyStoreConfigurationProperties;

	@Autowired
	KeyStoreProvider keyStoreProvider;


	@Test
	void loadOrCreateKeyStore_shouldCreateKeyStore_whenNotFound(@TempDir Path parentDirectory) {
		Path keyStorePath = keyStorePath(parentDirectory);


		keyStoreProvider.loadOrCreateKeyStore();


		assertThat(Files.exists(keyStorePath)).isTrue();
	}

	@Test
	void loadOrCreateKeyStore_shouldLoadPreviouslyCreatedKeyStore_whenFound(@TempDir Path parentDirectory)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateEncodingException {

		keyStorePath(parentDirectory);
		var jwtSigningKeyProperties = keyStoreConfigurationProperties.getJwtSigningKey();

		KeyStore createdKeyStore = keyStoreProvider.loadOrCreateKeyStore();
		var createdKeyStoreKey = createdKeyStore.getKey(jwtSigningKeyProperties.getAlias(),
				jwtSigningKeyProperties.getPassword().toCharArray());
		var createdKeyStoreCertificate = createdKeyStore.getCertificate(jwtSigningKeyProperties.getAlias());


		KeyStore loadedKeyStore = keyStoreProvider.loadOrCreateKeyStore();


		var loadedKeyStoreKey = createdKeyStore.getKey(jwtSigningKeyProperties.getAlias(),
				jwtSigningKeyProperties.getPassword().toCharArray());
		var loadedKeyStoreCertificate = loadedKeyStore.getCertificate(jwtSigningKeyProperties.getAlias());

		assertThat(createdKeyStoreKey.getEncoded()).isEqualTo(loadedKeyStoreKey.getEncoded());
		assertThat(createdKeyStoreCertificate.getEncoded()).isEqualTo(loadedKeyStoreCertificate.getEncoded());
		assertThat(createdKeyStoreCertificate.getPublicKey().getEncoded())
				.isEqualTo(loadedKeyStoreCertificate.getPublicKey().getEncoded());
	}


	private Path keyStorePath(Path parent) {
		Path result = parent.resolve("keyStore.p12");
		keyStoreConfigurationProperties.setPath(result);
		return result;
	}

}