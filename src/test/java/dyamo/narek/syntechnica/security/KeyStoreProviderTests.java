package dyamo.narek.syntechnica.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;

import static dyamo.narek.syntechnica.ConfigurationPropertiesBuilder.configProperties;
import static org.assertj.core.api.Assertions.assertThat;

class KeyStoreProviderTests {

	KeyStoreConfigurationProperties keyStoreConfigurationProperties;

	KeyStoreProvider keyStoreProvider;


	@BeforeEach
	void beforeEach() {
		keyStoreConfigurationProperties = configProperties(KeyStoreConfigurationProperties.class);
		keyStoreProvider = new KeyStoreProvider(keyStoreConfigurationProperties);
	}


	@Test
	void loadOrCreateKeyStore_shouldCreateKeyStore_whenNotFound(@TempDir Path tempParentDirectory) {
		Path keyStorePath = keyStorePath(tempParentDirectory);


		keyStoreProvider.loadOrCreateKeyStore();


		assertThat(Files.exists(keyStorePath)).isTrue();
	}

	@Test
	void loadOrCreateKeyStore_shouldLoadPreviouslyCreatedKeyStore_whenFound(@TempDir Path tempParentDirectory)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateEncodingException {

		keyStorePath(tempParentDirectory);
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
		Path originalPath = keyStoreConfigurationProperties.getPath();

		Path tempPath = parent.resolve(originalPath.getFileName());
		keyStoreConfigurationProperties.setPath(tempPath);

		return tempPath;
	}

}