package dyamo.narek.syntechnica.security.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialsTests {

	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


	@Test
	void validateUsername_shouldViolateConstraints_whenUsernameIsNull() {
		UserCredentials credentials = new UserCredentials(null, "password");


		Set<ConstraintViolation<UserCredentials>> violations = validator.validate(credentials);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validatePassword_shouldViolateConstraints_whenPasswordIsNull() {
		UserCredentials credentials = new UserCredentials("user", null);


		Set<ConstraintViolation<UserCredentials>> violations = validator.validate(credentials);


		assertThat(violations).isNotEmpty();
	}
}