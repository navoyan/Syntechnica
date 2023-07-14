package dyamo.narek.syntechnica.users;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static dyamo.narek.syntechnica.users.UserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;

class UserTests {
	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


	@ParameterizedTest
	@ValueSource(strings = {"not_hashed_password"})
	@NullSource
	void validatePassword_shouldViolateConstraints_whenPasswordIsNullOrNotHashed(String password) {
		User user = user().withPassword(password).build();


		Set<ConstraintViolation<User>> violations = validator.validate(user);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validatePassword_shouldSuccess_whenPasswordIsNotNullAndHashed() {
		User user = user().build();


		Set<ConstraintViolation<User>> violations = validator.validate(user);


		assertThat(violations).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"firstname--lastname", "-firstname-lastname",
			"firstname-lastname-", "firstname lastname"
	})
	@NullSource
	void validateName_shouldViolateConstraints_whenNameIsNullOrNotValidKebabCase(String name) {
		User user = user().withName(name).build();


		Set<ConstraintViolation<User>> violations = validator.validate(user);


		assertThat(violations).isNotEmpty();
	}

	@Test
	void validateName_shouldSuccess_whenNameIsNotNullAndValidKebabCase() {
		String name = "firstname-lastname-123";
		User user = user().withName(name).build();


		Set<ConstraintViolation<User>> violations = validator.validate(user);


		assertThat(violations).isEmpty();
	}

}