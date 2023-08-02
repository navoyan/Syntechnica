package dyamo.narek.syntechnica.security.auth.tokens.refresh;

import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTests {

	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void validateValue_shouldViolateConstraints_whenValueIsNull() {
		User user = user().withId().build();

		RefreshToken refreshToken = RefreshToken.builder()
				.value(null)
				.family(3L)
				.user(user)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateUser_shouldViolateConstraints_whenUserIsNull() {
		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(3L)
				.user(null)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateCreationTimestamp_shouldViolateConstraints_whenCreationTimestampIsNull() {
		User user = user().withId().build();

		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(3L)
				.user(user)
				.creationTimestamp(null)
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateExpirationTimestamp_shouldViolateConstraints_whenExpirationTimestampIsNull() {
		User user = user().withId().build();

		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(3L)
				.user(user)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(null)
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

}
