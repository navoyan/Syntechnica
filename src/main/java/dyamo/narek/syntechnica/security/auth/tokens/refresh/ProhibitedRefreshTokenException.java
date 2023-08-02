package dyamo.narek.syntechnica.security.auth.tokens.refresh;

public class ProhibitedRefreshTokenException extends InvalidRefreshTokenException {

	public ProhibitedRefreshTokenException(String message) {
		super(message);
	}

}
