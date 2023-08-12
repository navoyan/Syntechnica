package dyamo.narek.syntechnica.global;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public class RestDocumentationProviders {
	private static final String SCHEME = "https";
	private static final String HOST = "syntechnica.hostname.dev";

	private static final URI BASE_MOCKMVC_URI = URI.create(SCHEME + "://" + HOST);


	private RestDocumentationProviders() {}


	public static MockMvc docMockMvc(WebApplicationContext webApplicationContext,
									 RestDocumentationContextProvider restDocumentation) {
		return MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.apply(springSecurity())
				.apply(documentationConfiguration(restDocumentation)
						.uris()
							.withScheme(SCHEME).withHost(HOST).withPort(443)
						.and()
						.operationPreprocessors()
							.withRequestDefaults(
									prettyPrint(),
									modifyHeaders().remove("Content-Length").remove("Host")
							)
							.withResponseDefaults(
									prettyPrint(),
									modifyHeaders().remove("Content-Length").remove("Vary")
											.remove("X-Content-Type-Options").remove("Pragma")
											.remove("X-XSS-Protection").remove("Expires")
											.remove("X-Frame-Options").remove("Cache-Control")
											.remove("Strict-Transport-Security")
							)
				)
				.build();
	}


	public static String fullUri(URI against) {
		return BASE_MOCKMVC_URI.resolve(against).toString();
	}

	public static String fullUri(String against) {
		return BASE_MOCKMVC_URI.resolve(against).toString();
	}

	public static String fullUri(WebMvcLinkBuilder linkBuilder) {
		return fullUri(linkBuilder.toUri());
	}

	public static String fullUri(WebMvcLinkBuilder linkBuilder, String suffix) {
		return fullUri(linkBuilder) + suffix;
	}

	public static String baseUri() {
		return BASE_MOCKMVC_URI.toString();
	}

}
