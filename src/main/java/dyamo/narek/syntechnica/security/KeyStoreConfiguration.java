package dyamo.narek.syntechnica.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.Instant;
import java.time.Period;
import java.util.Date;

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
