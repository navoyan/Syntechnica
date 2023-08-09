package dyamo.narek.syntechnica.global.errors;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import dyamo.narek.syntechnica.global.TestAccessTokenProvider;
import dyamo.narek.syntechnica.global.errors.ErrorResponseAuthenticationEntryPointTests.TestController;
import dyamo.narek.syntechnica.users.TestUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static dyamo.narek.syntechnica.global.RestDocumentationProviders.docMockMvc;
import static dyamo.narek.syntechnica.global.RestDocumentationProviders.fullUri;
import static dyamo.narek.syntechnica.global.errors.ErrorResponseResultMatchers.errorResponse;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(TestController.class)
@ImportControllerConfiguration
@ExtendWith(RestDocumentationExtension.class)
class ErrorResponseAuthenticationEntryPointTests {

	MockMvc mockMvc;

	@Autowired
	TestAccessTokenProvider testAccessTokenProvider;


	@BeforeEach
	void beforeEach(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		TestUserBuilder.resetIndex();
		mockMvc = docMockMvc(webApplicationContext, restDocumentation);
	}


	@Test
	void commence_shouldPopulateErrorResponse_whenUnauthenticatedRequestIsPerformed() throws Exception {
		String securedEndpoint = "/secured";


		var actions = mockMvc.perform(post(securedEndpoint));


		actions.andExpect(errorResponse().status(HttpStatus.UNAUTHORIZED))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().exists())
				.andExpect(errorResponse().path().value(fullUri(securedEndpoint)));


		actions.andDo(document("error-unauthenticated",
				responseFields(
						fieldWithPath("timestamp").description("Error occurrence timestamp in ISO-8601 format"),
						fieldWithPath("statusCode").description("HTTP status code, always `401` in this case"),
						fieldWithPath("error").description("HTTP error that occurred, always `Unauthorized` in this case"),
						fieldWithPath("message").description("Description of the cause of the error"),
						fieldWithPath("path").description("Path to which the request was made")
				)));
	}

	@Test
	void commence_shouldDoNothing_whenAuthenticatedRequestIsPerformed() throws Exception {
		String securedEndpoint = "/secured";


		var actions = mockMvc.perform(post(securedEndpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		actions.andExpect(status().isOk());
	}


	@RestController
	static class TestController {

		@PostMapping("/secured")
		public ResponseEntity<?> securedEndpoint() {
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

}