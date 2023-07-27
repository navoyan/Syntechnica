package dyamo.narek.syntechnica.users.authorities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dyamo.narek.syntechnica.users.authorities.TestUserAuthorityBuilder.authority;
import static org.assertj.core.api.Assertions.assertThat;

class UserAuthorityTests {

	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


	@Test
	void validateType_shouldViolateConstraints_whenTypeIsNull() {
		UserAuthority authority = authority().withType(null).build();


		Set<ConstraintViolation<UserAuthority>> violations = validator.validate(authority);


		assertThat(violations).isNotEmpty();
	}

}