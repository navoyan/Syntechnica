package dyamo.narek.syntechnica.tokens;

import dyamo.narek.syntechnica.global.ImportControllerConfiguration;
import dyamo.narek.syntechnica.tokens.refresh.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static dyamo.narek.syntechnica.global.RestDocumentationProviders.docMockMvc;
import static dyamo.narek.syntechnica.global.RestDocumentationProviders.fullUri;
import static dyamo.narek.syntechnica.global.errors.ErrorResponseResultMatchers.errorResponse;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TokenController.class)
@ImportControllerConfiguration
@ExtendWith(RestDocumentationExtension.class)
class TokenControllerTests {

	@MockBean
	TokenPairService tokenPairService;

	MockMvc mockMvc;


	@BeforeEach
	void beforeEach(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		mockMvc = docMockMvc(webApplicationContext, restDocumentation);
	}


	@Test
	void generateTokenPairUsingCredentials_shouldGenerateTokens_whenProvidedCredentialsAreValid() throws Exception {
		String credentialsRequestBody = "{\"username\":\"user\",\"password\":\"password\"}";

		String encodedAccessToken = "eyJhbGciOiJSUzI1NiJ9" +
				".eyJpc3MiOiJzeW50ZWNobmljYSIsInN1YiI6ImFkbWluIiwiZXhwIjoxNjkxMDU5ODk1LC" +
				"JpYXQiOjE2OTEwNTI2OTUsInZlcnNpb24iOjEsImF1dGhvcml0aWVzIjpbIkFETUlOIl19" +
				".X2T5tQUAufTf3i0-1OkwU9w1kiq4pQYXd7B_g5DbVtgoxmvOVw7EysEK60rmh6_4gEtwTce7yfcWTyWJdYWO5Wc0d8kf7demMmm" +
				"AHEMQghIpICY4T3OqGhpxn8Fb1cSbheOdJiRv7Pui-nRrMidLK7AAcJocORk8JTsw05X9-v1PktoAPa7hpc7wHrm-Mfv-LfRQwTf" +
				"hx2Z61tsEnL1yHUR6mxY0GNkbDHkDR0h6BkBcf6xgC3-Go3ZOEhHHSUWT2IEesrAMAb4immo-GDlB-xetHJkEkcgn8imM8ax5n_B" +
				"ic4wbwVJMiSnxdsTa2kqqR6Bx9vrttWu83_A8hUZiPQ";

		var tokenResponse = new TokenPairResponse(encodedAccessToken, UUID.randomUUID());
		given(tokenPairService.generateTokens(any(UserCredentialsRequest.class))).willReturn(tokenResponse);


		var actions = mockMvc.perform(post("/tokens?grant_type=credentials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		actions.andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
				.andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken().toString()))
				.andExpect(jsonPath("$._links.tokens.href")
						.value(fullUri(linkTo(TokenController.class), "{?grant_type}")))
				.andExpect(status().isOk());
	}

	@Test
	void generateTokenPairUsingCredentials_shouldGiveErrorResponse_whenProvidedCredentialsAreInvalid() throws Exception {
		String endpoint = "/tokens?grant_type=credentials";

		String credentialsRequestBody = "{\"username\":\"invalid_username\",\"password\":\"invalid_password\"}";

		String exceptionMessage = "Invalid credentials";
		given(tokenPairService.generateTokens(any(UserCredentialsRequest.class)))
				.willThrow(new InvalidCredentialsException(exceptionMessage));


		var actions = mockMvc.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		actions.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(exceptionMessage))
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
	}

	@Test
	void generateTokenPairUsingCredentials_shouldGiveErrorResponse_whenProvidedCredentialsFailValidation()
			throws Exception {
		String endpoint = "/tokens?grant_type=credentials";

		String credentialsRequestBody = "{}";


		var actions = mockMvc.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentialsRequestBody));


		actions.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(startsWith("Validation failed")))
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
	}


	@Test
	void generateTokensUsingRefreshToken_shouldGenerateTokens_whenProvidedRefreshTokenIsValid() throws Exception {
		UUID providedValidRefreshToken = UUID.randomUUID();
		String refreshTokenRequestBody = "{\"refreshToken\":\"" + providedValidRefreshToken + "\"}";

		String encodedAccessToken = "eyJhbGciOiJSUzI1NiJ9" +
				".eyJpc3MiOiJzeW50ZWNobmljYSIsInN1YiI6ImFkbWluIiwiZXhwIjoxNjkxMDU5ODk1LC" +
				"JpYXQiOjE2OTEwNTI2OTUsInZlcnNpb24iOjEsImF1dGhvcml0aWVzIjpbIkFETUlOIl19" +
				".X2T5tQUAufTf3i0-1OkwU9w1kiq4pQYXd7B_g5DbVtgoxmvOVw7EysEK60rmh6_4gEtwTce7yfcWTyWJdYWO5Wc0d8kf7demMmm" +
				"AHEMQghIpICY4T3OqGhpxn8Fb1cSbheOdJiRv7Pui-nRrMidLK7AAcJocORk8JTsw05X9-v1PktoAPa7hpc7wHrm-Mfv-LfRQwTf" +
				"hx2Z61tsEnL1yHUR6mxY0GNkbDHkDR0h6BkBcf6xgC3-Go3ZOEhHHSUWT2IEesrAMAb4immo-GDlB-xetHJkEkcgn8imM8ax5n_B" +
				"ic4wbwVJMiSnxdsTa2kqqR6Bx9vrttWu83_A8hUZiPQ";

		var tokenResponse = new TokenPairResponse(encodedAccessToken, UUID.randomUUID());
		given(tokenPairService.generateTokens(any(UUID.class))).willReturn(tokenResponse);


		var actions = mockMvc.perform(post("/tokens?grant_type=refresh_token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshTokenRequestBody));


		actions.andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
				.andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken().toString()))
				.andExpect(jsonPath("$._links.tokens.href")
						.value(fullUri(linkTo(TokenController.class), "{?grant_type}")))
				.andExpect(status().isOk());
	}

	@Test
	void generateTokensUsingRefreshToken_shouldGiveErrorResponse_whenProvidedRefreshTokenIsInvalid() throws Exception {
		String endpoint = "/tokens?grant_type=refresh_token";


		UUID providedInvalidRefreshToken = UUID.randomUUID();
		String refreshTokenRequestBody = "{\"refreshToken\":\"" + providedInvalidRefreshToken + "\"}";

		String exceptionMessage = "Invalid refresh token";
		given(tokenPairService.generateTokens(any(UUID.class)))
				.willThrow(new InvalidRefreshTokenException(exceptionMessage));


		var actions = mockMvc.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshTokenRequestBody));


		actions.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(exceptionMessage))
				.andExpect(errorResponse().path().value(fullUri(endpoint)));
	}

	@Test
	void generateTokensUsingRefreshToken_shouldGiveErrorResponse_whenProvidedRefreshTokenRequestFailsValidation()
			throws Exception {
		String endpoint = "/tokens?grant_type=refresh_token";

		String refreshTokenRequestBody = "{}";


		var actions = mockMvc.perform(post(endpoint)
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshTokenRequestBody));
 

		actions.andExpect(errorResponse().status(HttpStatus.BAD_REQUEST))
				.andExpect(errorResponse().validTimestamp())
				.andExpect(errorResponse().message().value(startsWith("Validation failed")))
				.andExpect(errorResponse().path().value(fullUri(endpoint)));

	}

}