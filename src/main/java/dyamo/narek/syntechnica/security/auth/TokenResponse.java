package dyamo.narek.syntechnica.security.auth;

import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Data
public class TokenResponse {

	@NonNull
	private final String accessToken;

	@NonNull
	private final UUID refreshToken;

}
