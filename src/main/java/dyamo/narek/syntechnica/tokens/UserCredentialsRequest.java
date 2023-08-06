package dyamo.narek.syntechnica.tokens;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentialsRequest {

	@NotNull
	private String username;

	@NotNull
	private String password;

}
