package dyamo.narek.syntechnica.global;

import dyamo.narek.syntechnica.tokens.TokenController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static dyamo.narek.syntechnica.global.RestDocumentationProviders.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(IndexController.class)
@ImportControllerConfiguration
@ExtendWith(RestDocumentationExtension.class)
class IndexControllerTests {

	MockMvc mockMvc;

	@Autowired
	TestAccessTokenProvider testAccessTokenProvider;


	@BeforeEach
	void beforeEach(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		mockMvc = docMockMvc(webApplicationContext, restDocumentation);
	}


	@Test
	void index_shouldGiveHypermediaLinksResponse() throws Exception {
		String endpoint = "/";


		var actions = mockMvc.perform(get(endpoint));


		actions.andExpect(jsonPath("$._links.self.href").value(baseUri()))
				.andExpect(jsonPath("$._links.tokens.href")
						.value(fullUri(linkTo(TokenController.class), "{?grant_type}")));


		actions.andDo(document("index-access",
				responseFields(
						subsectionWithPath("_links").description("<<<resource_index_access_links, Links>> to other resources")
				),
				links(
						linkWithRel("self").description("<<resource_index, Index>> resource"),
						linkWithRel("tokens").description("<<resource_tokens, Tokens>> resource")
				)));
	}
}