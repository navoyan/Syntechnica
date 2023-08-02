package dyamo.narek.syntechnica.global.errors;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import dyamo.narek.syntechnica.global.TestAccessTokenProvider;
import dyamo.narek.syntechnica.global.errors.ErrorResponseAuthenticationEntryPointTests.TestController;
import dyamo.narek.syntechnica.users.TestUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static dyamo.narek.syntechnica.global.AssertionMatchers.matchAssertion;
import static dyamo.narek.syntechnica.global.errors.ErrorResponseResultMatchers.errorResponse;
import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(TestController.class)
@ImportControllerConfiguration
class ErrorResponseAuthenticationEntryPointTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TestAccessTokenProvider testAccessTokenProvider;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void commence_shouldPopulateErrorResponse_whenUnauthenticatedRequestIsPerformed() throws Exception {
		String securedEndpoint = "/secured";


		var perform = mockMvc.perform(post(securedEndpoint));


		perform.andExpect(errorResponse().status(HttpStatus.UNAUTHORIZED))
				.andExpect(content().contentType(MediaTypes.HAL_JSON))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(matchAssertion((String message) -> {
					assertThat(message).isNotEmpty();
				})))
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith(securedEndpoint);
				})));
	}

	@Test
	void commence_shouldDoNothing_whenAuthenticatedRequestIsPerformed() throws Exception {
		String securedEndpoint = "/secured";


		var perform = mockMvc.perform(post(securedEndpoint)
				.with(testAccessTokenProvider.bearerAccessToken(user().build())));


		perform.andExpect(status().isOk());
	}


	@RestController
	static class TestController {

		@PostMapping("/secured")
		public ResponseEntity<?> securedEndpoint() {
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

}