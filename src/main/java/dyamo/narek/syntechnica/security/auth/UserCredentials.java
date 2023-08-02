package dyamo.narek.syntechnica.security.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentials {

	@NotNull
	private String username;

	@NotNull
	private String password;

}
