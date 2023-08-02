package dyamo.narek.syntechnica.security.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRequestTests {

	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


	@Test
	void validateRefreshToken_shouldViolateConstraints_whenRefreshTokenIsNull() {
		RefreshTokenRequest credentials = new RefreshTokenRequest(null);


		Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(credentials);


		assertThat(violations).isNotEmpty();
	}

}