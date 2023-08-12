package dyamo.narek.syntechnica.global;

import dyamo.narek.syntechnica.global.errors.ErrorResponseAuthenticationEntryPoint;
import dyamo.narek.syntechnica.security.*;
import dyamo.narek.syntechnica.tokens.access.AccessTokenConfigurationProperties;
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
		TestAuthConfiguration.class,
		KeyStoreProvider.class,
		ValidFamilyJwtAuthenticationProvider.class, VersionedJwtAuthenticationProvider.class,
		ErrorResponseAuthenticationEntryPoint.class,
		KeyStoreConfigurationProperties.class, AccessTokenConfigurationProperties.class
})
public @interface ImportSecurityConfiguration {
}
