package dyamo.narek.syntechnica.global.errors;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import dyamo.narek.syntechnica.global.TestAccessTokenProvider;
import dyamo.narek.syntechnica.global.errors.ErrorControllerAdviceTests.TestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static dyamo.narek.syntechnica.global.AssertionMatchers.matchAssertion;
import static dyamo.narek.syntechnica.global.errors.ErrorResponseResultMatchers.errorResponse;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(TestController.class)
@Import(TestController.class)
@ImportControllerConfiguration
class ErrorControllerAdviceTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TestAccessTokenProvider testAccessTokenProvider;


	@Test
	void handleUnresolvedException_shouldGiveErrorResponse_whenNotPermittedExceptionIsThrown() throws Exception {
		String endpoint = "/not_permitted";


		var perform = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		perform.andExpect(errorResponse().status(HttpStatus.INTERNAL_SERVER_ERROR))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().doesNotExist())
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith(endpoint);
				})));
	}

	@Test
	void handlePermittedException_shouldGiveErrorResponse_whenPermittedExceptionIsThrown() throws Exception {
		String endpoint = "/permitted";


		var perform = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		perform.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value("PERMITTED EXCEPTION MESSAGE"))
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith(endpoint);
				})));
	}

	@Test
	void handleAccessDeniedException_shouldGiveErrorResponse_whenUnauthorizedRequestIsPerformed() throws Exception {
		String endpoint = "/protected";


		var perform = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		perform.andExpect(errorResponse().status(HttpStatus.FORBIDDEN))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value("Access Denied"))
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith(endpoint);
				})));
	}

	@Test
	void handleValidationException_shouldGiveErrorResponse_whenRequest() throws Exception {
		String endpoint = "/validating";

		String invalidRequestBody = "{}";


		var perform = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequestBody));


		perform.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().exists())
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith(endpoint);
				})));
	}



	@RestController
	static class TestController {

		record TestRequestBody(@NotNull String data) {}


		@PostMapping("/not_permitted")
		public ResponseEntity<?> endpointNotPermittedThrowing() {
			throw new IllegalStateException("IGNORED MESSAGE");
		}

		@PostMapping("/permitted")
		public ResponseEntity<?> endpointPermittedThrowing() {
			throw new DefaultHandledException(
					HttpStatus.BAD_REQUEST,
					new IllegalStateException("PERMITTED EXCEPTION MESSAGE")
			);
		}

		@PostMapping("/protected")
		@PreAuthorize("hasAuthority('ADMIN')")
		public ResponseEntity<?> endpointProtected() {
			return new ResponseEntity<>(HttpStatus.OK);
		}

		@PostMapping("/validating")
		public ResponseEntity<?> endpointValidating(@Valid @RequestBody TestRequestBody requestBody) {
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

}