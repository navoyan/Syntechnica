package dyamo.narek.syntechnica.global.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

	private final LocalDateTime timestamp = LocalDateTime.now(Clock.systemUTC());
	private final int statusCode;
	private final String error;
	private CharSequence message;


	public ErrorResponse(HttpStatus status) {
		this.statusCode = status.value();
		this.error = status.getReasonPhrase();
	}

	public ErrorResponse(HttpStatus status, CharSequence message) {
		this(status);
		this.message = message;
	}

}
