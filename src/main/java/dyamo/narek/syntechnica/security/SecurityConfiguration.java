package dyamo.narek.syntechnica.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	public static final String BCRYPT_HASH_PATTERN = "\\$2a\\$10\\$[./0-9A-Za-z]{53}";


	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
												   AuthenticationManager authenticationManager
	) throws Exception {
		return http
				.cors(withDefaults())
				.csrf(CsrfConfigurer::disable)
				.sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/tokens").permitAll();
					auth.anyRequest().authenticated();
				})
				.oauth2ResourceServer(resourceServer -> {
					resourceServer.jwt(jwt -> {
						jwt.authenticationManager(authenticationManager);
					});
				})
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
