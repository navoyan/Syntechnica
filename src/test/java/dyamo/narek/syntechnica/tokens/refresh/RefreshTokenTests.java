package dyamo.narek.syntechnica.tokens.refresh;

import dyamo.narek.syntechnica.tokens.family.TokenFamily;
import dyamo.narek.syntechnica.users.TestUserBuilder;
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
		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.build();

		RefreshToken refreshToken = RefreshToken.builder()
				.value(null)
				.family(tokenFamily)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateUser_shouldViolateConstraints_whenFamilyIsNull() {
		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(null)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateCreationTimestamp_shouldViolateConstraints_whenCreationTimestampIsNull() {
		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.build();

		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(tokenFamily)
				.creationTimestamp(null)
				.expirationTimestamp(Instant.now().plus(1, ChronoUnit.HOURS))
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateExpirationTimestamp_shouldViolateConstraints_whenExpirationTimestampIsNull() {
		var tokenFamily = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.build();

		RefreshToken refreshToken = RefreshToken.builder()
				.value(UUID.randomUUID())
				.family(tokenFamily)
				.creationTimestamp(Instant.now())
				.expirationTimestamp(null)
				.build();


		Set<ConstraintViolation<RefreshToken>> violations = validator.validate(refreshToken);


		assertThat(violations).isNotEmpty();
	}

}
