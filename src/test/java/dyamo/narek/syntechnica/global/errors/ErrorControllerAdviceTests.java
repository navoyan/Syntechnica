package dyamo.narek.syntechnica.global.errors;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import dyamo.narek.syntechnica.global.TestAccessTokenProvider;
import dyamo.narek.syntechnica.global.errors.ErrorControllerAdviceTests.TestController;
import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static dyamo.narek.syntechnica.global.RestDocumentationProviders.docMockMvc;
import static dyamo.narek.syntechnica.global.RestDocumentationProviders.fullUri;
import static dyamo.narek.syntechnica.global.errors.ErrorResponseResultMatchers.errorResponse;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static dyamo.narek.syntechnica.users.authorities.TestUserAuthorityBuilder.authority;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(TestController.class)
@Import(TestController.class)
@ImportControllerConfiguration
@ExtendWith(RestDocumentationExtension.class)
class ErrorControllerAdviceTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TestAccessTokenProvider testAccessTokenProvider;


	@BeforeEach
	void beforeEach(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		TestUserBuilder.resetIndex();
		mockMvc = docMockMvc(webApplicationContext, restDocumentation);
	}


	@Test
	void handleUnresolvedException_shouldGiveErrorResponse_whenNotPermittedExceptionIsThrown() throws Exception {
		String endpoint = "/not_permitted";


		var actions = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		actions.andExpect(errorResponse().status(HttpStatus.INTERNAL_SERVER_ERROR))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().doesNotExist())
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
	}

	@Test
	void handlePermittedException_shouldGiveErrorResponse_whenPermittedExceptionIsThrown() throws Exception {
		String endpoint = "/permitted";


		var actions = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		actions.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value("ERROR"))
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
	}

	@Test
	void handleAccessDeniedException_shouldGiveErrorResponse_whenUnauthorizedRequestIsPerformed() throws Exception {
		String endpoint = "/protected";


		var actions = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(
						user().withAuthorities(authority().withType(UserAuthorityType.ADMIN).build()).build()
				)));


		actions.andExpect(errorResponse().status(HttpStatus.FORBIDDEN))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value("Access Denied"))
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
	}

	@Test
	void handleValidationException_shouldGiveErrorResponse_whenRequest() throws Exception {
		String endpoint = "/validating";

		String invalidRequestBody = "{}";


		var actions = mockMvc.perform(post(endpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequestBody));


		actions.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().exists())
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
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
					new IllegalStateException("ERROR")
			);
		}

		@PostMapping("/protected")
		@PreAuthorize("hasAuthority('ADMIN')")
		public ResponseEntity<?> endpointProtected() {
			return new ResponseEntity<>(HttpStatus.OK);
		}

		@PostMapping("/validating")
		public ResponseEntity<?> endpointValidating(@RequestBody @Valid TestRequestBody requestBody) {
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

}