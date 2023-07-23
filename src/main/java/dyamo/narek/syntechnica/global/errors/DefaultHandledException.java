package dyamo.narek.syntechnica.global.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class DefaultHandledException extends RuntimeException {

	@Getter
	private final HttpStatus responseStatus;


	public DefaultHandledException(HttpStatus responseStatus, Throwable cause) {
		super(cause);
		this.responseStatus = responseStatus;
	}

}
