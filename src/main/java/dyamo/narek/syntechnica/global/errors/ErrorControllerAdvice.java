package dyamo.narek.syntechnica.global.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorControllerAdvice {

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleUnresolvedException(HttpServletRequest request) {
		URI path = getRequestUriBuilder(request).build().toUri();

		return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, path);
	}

	@ExceptionHandler(DefaultHandledException.class)
	public ResponseEntity<ErrorResponse> handlePermittedException(DefaultHandledException exception,
																  HttpServletRequest request) {
		URI path = getRequestUriBuilder(request).build().toUri();

		Throwable original = exception.getCause();
		HttpStatus responseStatus = exception.getResponseStatus();

		var errorResponse = new ErrorResponse(responseStatus, path, original.getMessage());

		return new ResponseEntity<>(errorResponse, responseStatus);
	}


	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleAccessDeniedException(AccessDeniedException exception,
													 HttpServletRequest request) {
		URI path = getRequestUriBuilder(request).build().toUri();

		return new ErrorResponse(HttpStatus.FORBIDDEN, path, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidationException(MethodArgumentNotValidException exception,
												   HttpServletRequest request) {
		URI path = getRequestUriBuilder(request).build().toUri();

		String message = "Validation failed for: "
				+ exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + " (" + error.getDefaultMessage() + ")")
				.collect(Collectors.joining(", "));

		return new ErrorResponse(HttpStatus.BAD_REQUEST, path, message);
	}


	private UriComponentsBuilder getRequestUriBuilder(HttpServletRequest request) {
		return ServletUriComponentsBuilder.fromRequest(request);
	}

}
