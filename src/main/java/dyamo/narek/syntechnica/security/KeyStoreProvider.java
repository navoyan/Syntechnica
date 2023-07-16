package dyamo.narek.syntechnica.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.Instant;
import java.time.Period;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyStoreProvider {

	@Value("CN=${app.name}")
	private String applicationDN;

	private final KeyStoreConfigurationProperties properties;


	@SneakyThrows
	public KeyStore loadOrCreateKeyStore() {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
		Path keyStorePath = properties.getPath();

		if (Files.exists(keyStorePath)) {
			loadKeyStore(keyStore, keyStorePath);
		}
		else {
			populateKeyStore(keyStore, keyStorePath);
		}

		return keyStore;
	}



	@SneakyThrows
	private void loadKeyStore(KeyStore keyStore, Path path) {
		try (var keyStoreDataStream = Files.newInputStream(path, StandardOpenOption.READ)) {
			keyStore.load(keyStoreDataStream, properties.getPassword().toCharArray());
		}

		log.info("Using KeyStore located in " + path.toAbsolutePath());
	}

	@SneakyThrows
	private void populateKeyStore(KeyStore keyStore, Path path) {
		keyStore.load(null, properties.getPassword().toCharArray());

		SecureRandom random = new SecureRandom();

		KeyPair jwtKeys = generateRSAKeyPair(random);
		Certificate[] certificateChain = { createCertificate(jwtKeys.getPublic(), jwtKeys.getPrivate(), random) };
		keyStore.setKeyEntry(properties.getJwtSigningKey().getAlias(), jwtKeys.getPrivate(),
				properties.getJwtSigningKey().getPassword().toCharArray(), certificateChain);

		Files.createDirectories(path.getParent());

		try	(var keyStoreOutputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			keyStore.store(keyStoreOutputStream, properties.getPassword().toCharArray());
		}

		log.info("KeyStore created at " + path.toAbsolutePath());
	}

	@SneakyThrows
	private KeyPair generateRSAKeyPair(SecureRandom random) {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA",
				BouncyCastleProvider.PROVIDER_NAME);
		keyPairGenerator.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4), random);

		return keyPairGenerator.generateKeyPair();
	}

	@SneakyThrows
	private X509Certificate createCertificate(PublicKey certificateKey, PrivateKey signingKey, SecureRandom random) {
		BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

		X500Principal issuerAndSubject = new X500Principal(applicationDN);

		Instant now = Instant.now();
		Date notBefore = Date.from(now);
		Date notAfter = Date.from(now.plus(Period.ofDays(100 * 365)));

		X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
				issuerAndSubject, serialNumber, notBefore, notAfter, issuerAndSubject, certificateKey
		);

		certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(0));


		JcaX509CertificateConverter converter = new JcaX509CertificateConverter()
				.setProvider(BouncyCastleProvider.PROVIDER_NAME);

		ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
				.setSecureRandom(random)
				.setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.build(signingKey);

		return converter.getCertificate(certificateBuilder.build(signer));
	}

}
