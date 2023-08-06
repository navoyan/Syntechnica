package dyamo.narek.syntechnica.tokens.refresh;

public class ProhibitedRefreshTokenException extends InvalidRefreshTokenException {

	public ProhibitedRefreshTokenException(String message) {
		super(message);
	}

}
