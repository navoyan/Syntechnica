package dyamo.narek.syntechnica.global;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.Link;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkProviders {

	public static Link completed(Link link, String template) {
		return Link.of(link.toUri() + template, link.getRel());
	}

}
