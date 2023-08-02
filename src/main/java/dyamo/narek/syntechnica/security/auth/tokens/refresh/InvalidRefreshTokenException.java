package dyamo.narek.syntechnica.security.auth.tokens.refresh;

public class InvalidRefreshTokenException extends RuntimeException {

	public InvalidRefreshTokenException(String message) {
		super(message);
	}

}
