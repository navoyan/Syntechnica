package dyamo.narek.syntechnica.tokens;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialsRequestTests {

	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


	@Test
	void validateUsername_shouldViolateConstraints_whenUsernameIsNull() {
		UserCredentialsRequest credentials = new UserCredentialsRequest(null, "password");


		Set<ConstraintViolation<UserCredentialsRequest>> violations = validator.validate(credentials);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validatePassword_shouldViolateConstraints_whenPasswordIsNull() {
		UserCredentialsRequest credentials = new UserCredentialsRequest("user", null);


		Set<ConstraintViolation<UserCredentialsRequest>> violations = validator.validate(credentials);


		assertThat(violations).isNotEmpty();
	}
}