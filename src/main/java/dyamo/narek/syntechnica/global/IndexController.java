package dyamo.narek.syntechnica.global;

import dyamo.narek.syntechnica.tokens.TokenController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dyamo.narek.syntechnica.global.LinkProviders.completed;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/")
public class IndexController {

	@GetMapping
	public RepresentationModel<?> index() {
		return CollectionModel.empty(
				linkTo(IndexController.class).withSelfRel(),
				completed(linkTo(TokenController.class).withRel("tokens"), "{?grant_type}")
		);
	}

}
