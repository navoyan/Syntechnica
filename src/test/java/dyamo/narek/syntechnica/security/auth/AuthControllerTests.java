package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static dyamo.narek.syntechnica.global.AssertionMatchers.matchAssertion;
import static dyamo.narek.syntechnica.global.errors.ErrorResponseResultMatchers.errorResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ImportControllerConfiguration
class AuthControllerTests {

	@MockBean
	AuthService authService;

	@Autowired
	MockMvc mockMvc;


	@Test
	void generateTokensWithCredentials_shouldGenerateTokens_whenProvidedCredentialsAreValid() throws Exception {
		String credentialsRequestBody = "{\"username\":\"user\",\"password\":\"password\"}";

		var tokenResponse = new TokenResponse("ENCODED ACCESS TOKEN");
		given(authService.generateTokens(any())).willReturn(tokenResponse);


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
				.andExpect(jsonPath("$._links.self.href").value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})))
				.andExpect(status().isOk());
	}

	@Test
	void generateTokensWithCredentials_shouldGiveErrorResponse_whenProvidedUsernameNotFound() throws Exception {
		String credentialsRequestBody = "{\"username\":\"not_existing\",\"password\":\"password\"}";

		String exceptionMessage = "USERNAME NOT FOUND MESSAGE";
		given(authService.generateTokens(any())).willThrow(new UsernameNotFoundException(exceptionMessage));


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(errorResponse().status(HttpStatus.UNAUTHORIZED))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message(exceptionMessage))
				.andExpect(errorResponse().selfRef().value(matchAssertion(selfRef -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})));
	}

	@Test
	void generateTokensWithCredentials_shouldGiveErrorResponse_whenProvidedPasswordDoesNotMatch() throws Exception {
		String credentialsRequestBody = "{\"username\":\"user\",\"password\":\"invalid_password\"}";

		String exceptionMessage = "BAD CREDENTIALS MESSAGE";
		given(authService.generateTokens(any())).willThrow(new BadCredentialsException(exceptionMessage));


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(errorResponse().status(HttpStatus.UNAUTHORIZED))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message(exceptionMessage))
				.andExpect(errorResponse().selfRef().value(matchAssertion(selfRef -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})));
	}

	@Test
	void generateTokensWithCredentials_shouldGiveErrorResponse_whenProvidedCredentialsFailValidation() throws Exception {
		String credentialsRequestBody = "{}";


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message(matchAssertion(message -> {
					assertThat(message).startsWith("Validation failed");
				})))
				.andExpect(errorResponse().selfRef().value(matchAssertion(selfRef -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})));
	}

}