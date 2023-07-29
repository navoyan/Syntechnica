package dyamo.narek.syntechnica.global.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@Component
public class ErrorResponseAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;


	public ErrorResponseAuthenticationEntryPoint(ObjectMapper objectMapper, HypermediaMappingInformation halMediaTypeConfiguration) {
		this.objectMapper = halMediaTypeConfiguration.configureObjectMapper(objectMapper.copy());
	}


	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		String uri = ServletUriComponentsBuilder.fromRequest(request).build().toUriString();

		EntityModel<ErrorResponse> errorResponseModel = EntityModel.of(
				new ErrorResponse(status, authException.getMessage()),
				Link.of(uri).withSelfRel()
		);

		response.setStatus(status.value());
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), errorResponseModel);
	}

}
