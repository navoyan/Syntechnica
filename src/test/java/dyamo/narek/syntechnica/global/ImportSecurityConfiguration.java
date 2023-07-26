package dyamo.narek.syntechnica.global;

import dyamo.narek.syntechnica.security.KeyStoreConfiguration;
import dyamo.narek.syntechnica.security.KeyStoreConfigurationProperties;
import dyamo.narek.syntechnica.security.KeyStoreProvider;
import dyamo.narek.syntechnica.security.SecurityConfiguration;
import dyamo.narek.syntechnica.security.auth.AuthConfiguration;
import dyamo.narek.syntechnica.security.auth.tokens.JwtConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({
		SecurityConfiguration.class,
		AuthConfiguration.class, KeyStoreConfiguration.class,
		KeyStoreProvider.class,
		KeyStoreConfigurationProperties.class, JwtConfigurationProperties.class
})
public @interface ImportSecurityConfiguration {
}
