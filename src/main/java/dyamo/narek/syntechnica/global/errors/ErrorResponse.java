package dyamo.narek.syntechnica.global.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

	@NonNull
	private final LocalDateTime timestamp = LocalDateTime.now(Clock.systemUTC());

	private final int statusCode;

	@NonNull
	private final String error;

	@Nullable
	private CharSequence message;

	@NonNull
	private URI path;


	public ErrorResponse(@NonNull HttpStatus status, @NonNull URI path) {
		this.statusCode = status.value();
		this.error = status.getReasonPhrase();
		this.path = path;
	}

	public ErrorResponse(@NonNull HttpStatus status, @NonNull URI path, @Nullable CharSequence message) {
		this(status, path);
		this.message = message;
	}

}
