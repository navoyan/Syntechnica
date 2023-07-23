package dyamo.narek.syntechnica.global.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorControllerAdvice {

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public EntityModel<ErrorResponse> handleUnresolvedException(HttpServletRequest request) {
		String uri = getRequestUriBuilder(request).build().toUriString();

		return EntityModel.of(
				new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR),
				Link.of(uri).withSelfRel()
		);
	}

	@ExceptionHandler(DefaultHandledException.class)
	public ResponseEntity<EntityModel<ErrorResponse>> handlePermittedException(DefaultHandledException exception,
																					HttpServletRequest request) {
		String uri = getRequestUriBuilder(request).build().toUriString();

		Throwable original = exception.getCause();
		HttpStatus responseStatus = exception.getResponseStatus();

		var entityModel = EntityModel.of(
				new ErrorResponse(responseStatus, original.getMessage()),
				Link.of(uri).withSelfRel()
		);

		return new ResponseEntity<>(entityModel, responseStatus);
	}


	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public EntityModel<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String uri = getRequestUriBuilder(request).build().toUriString();

		String message = "Validation failed for: "
				+ exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + " (" + error.getDefaultMessage() + ")")
				.collect(Collectors.joining(", "));

		return EntityModel.of(
				new ErrorResponse(HttpStatus.BAD_REQUEST, message),
				Link.of(uri).withSelfRel()
		);
	}


	private UriComponentsBuilder getRequestUriBuilder(HttpServletRequest request) {
		return ServletUriComponentsBuilder.fromRequest(request);
	}

}
