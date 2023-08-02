package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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
	void generateTokensUsingCredentials_shouldGenerateTokens_whenProvidedCredentialsAreValid() throws Exception {
		String credentialsRequestBody = "{\"username\":\"user\",\"password\":\"password\"}";

		var tokenResponse = new TokenResponse("ENCODED ACCESS TOKEN", UUID.randomUUID());
		given(authService.generateTokens(any(UserCredentials.class))).willReturn(tokenResponse);


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
				.andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken().toString()))
				.andExpect(jsonPath("$._links.self.href").value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})))
				.andExpect(status().isOk());
	}

	@Test
	void generateTokensUsingCredentials_shouldGiveErrorResponse_whenProvidedCredentialsAreInvalid() throws Exception {
		String credentialsRequestBody = "{\"username\":\"invalid_username\",\"password\":\"invalid_password\"}";

		String exceptionMessage = "INVALID CREDENTIALS MESSAGE";
		given(authService.generateTokens(any(UserCredentials.class)))
				.willThrow(new InvalidCredentialsException(exceptionMessage));


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(errorResponse().status(HttpStatus.UNAUTHORIZED))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(exceptionMessage))
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})));
	}

	@Test
	void generateTokensUsingCredentials_shouldGiveErrorResponse_whenProvidedCredentialsFailValidation()
			throws Exception {
		String credentialsRequestBody = "{}";


		var perform = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		perform.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(matchAssertion((String message) -> {
					assertThat(message).startsWith("Validation failed");
				})))
				.andExpect(errorResponse().selfRef().value(matchAssertion((String selfRef) -> {
					assertThat(selfRef).endsWith("/tokens?grant_type=credentials");
				})));
	}

}