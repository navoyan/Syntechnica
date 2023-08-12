package dyamo.narek.syntechnica.global.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class ErrorResponseAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;


	@Override
	public void commence(HttpServletRequest request,
						 HttpServletResponse response,
						 AuthenticationException authException) throws IOException {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		URI path = ServletUriComponentsBuilder.fromRequest(request).build().toUri();

		ErrorResponse errorResponse = new ErrorResponse(status, path, authException.getMessage());

		response.setStatus(status.value());
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), errorResponse);
	}

}
