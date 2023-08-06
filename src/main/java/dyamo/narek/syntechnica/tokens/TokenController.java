package dyamo.narek.syntechnica.tokens;

import dyamo.narek.syntechnica.global.errors.DefaultExceptionHandler;
import dyamo.narek.syntechnica.tokens.refresh.InvalidRefreshTokenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static dyamo.narek.syntechnica.global.LinkProviders.completed;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
public class TokenController {

	private final TokenPairService tokenPairService;


	@PostMapping(params = "grant_type=credentials")
	@ResponseStatus(HttpStatus.OK)
	@DefaultExceptionHandler(
			responseStatus = HttpStatus.BAD_REQUEST,
			exceptions = InvalidCredentialsException.class
	)
	public EntityModel<TokenPairResponse> generateTokenPairUsingCredentials(
			@RequestBody @Valid UserCredentialsRequest credentials
	) {
		TokenPairResponse tokenPairResponse = tokenPairService.generateTokens(credentials);

		return EntityModel.of(tokenPairResponse,
				completed(linkTo(TokenController.class).withRel("tokens"),"{?grant_type}")
		);
	}


	@PostMapping(params = "grant_type=refresh_token")
	@ResponseStatus(HttpStatus.OK)
	@DefaultExceptionHandler(
			responseStatus = HttpStatus.BAD_REQUEST,
			exceptions = InvalidRefreshTokenException.class
	)
	public EntityModel<TokenPairResponse> generateTokenPairUsingRefreshToken(
			@RequestBody @Valid RefreshTokenRequest refreshTokenRequest
	) {
		TokenPairResponse tokenPairResponse = tokenPairService.generateTokens(refreshTokenRequest.getRefreshToken());

		return EntityModel.of(tokenPairResponse,
				completed(linkTo(TokenController.class).withRel("tokens"),"{?grant_type}")
		);
	}

}
