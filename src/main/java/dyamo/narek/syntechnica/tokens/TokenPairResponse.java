package dyamo.narek.syntechnica.tokens;

import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Data
public class TokenPairResponse {

	@NonNull
	private final String accessToken;

	@NonNull
	private final UUID refreshToken;

}
