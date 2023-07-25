package dyamo.narek.syntechnica.security.auth;

import dyamo.narek.syntechnica.global.errors.DefaultExceptionHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;


	@PostMapping(value = "/tokens", params = "grant_type=credentials")
	@ResponseStatus(HttpStatus.OK)
	@DefaultExceptionHandler(
			responseStatus = HttpStatus.UNAUTHORIZED,
			exceptions = {UsernameNotFoundException.class, BadCredentialsException.class}
	)
	public EntityModel<TokenResponse> generateTokensUsingCredentials(@RequestBody @Valid UserCredentials credentials) {
		TokenResponse tokenResponse = authService.generateTokens(credentials);

		return EntityModel.of(tokenResponse,
				linkTo(methodOn(AuthController.class).generateTokensUsingCredentials(credentials)).withSelfRel()
		);
	}

}
