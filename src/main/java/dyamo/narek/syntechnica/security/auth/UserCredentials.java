package dyamo.narek.syntechnica.security.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCredentials {

	@NotNull
	private final String username;

	@NotNull
	private final String password;

}
