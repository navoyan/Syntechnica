package dyamo.narek.syntechnica.global.errors;

import org.hamcrest.Matcher;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static dyamo.narek.syntechnica.global.AssertionMatchers.matchAssertion;
import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseResultMatchers {

	private static final ErrorResponseResultMatchers instance = new ErrorResponseResultMatchers();


	public static ErrorResponseResultMatchers errorResponse() {
		return instance;
	}


	private ErrorResponseResultMatchers() {}


	public ResultMatcher status(HttpStatus httpStatus) {
		return result -> {
			MockMvcResultMatchers.status().is(httpStatus.value()).match(result);
			MockMvcResultMatchers.jsonPath("$.statusCode").value(httpStatus.value()).match(result);
			MockMvcResultMatchers.jsonPath("$.error").value(httpStatus.getReasonPhrase()).match(result);
		};
	}

	public ResultMatcher status(Matcher<HttpStatus> httpStatusMatcher) {
		return result -> {
			HttpStatus responseStatus = HttpStatus.valueOf(result.getResponse().getStatus());

			assertThat(httpStatusMatcher.matches(responseStatus)).isTrue();
			MockMvcResultMatchers.jsonPath("$.statusCode").value(responseStatus.value()).match(result);
			MockMvcResultMatchers.jsonPath("$.error").value(responseStatus.getReasonPhrase()).match(result);
		};
	}

	public ResultMatcher message(String message) {
		return MockMvcResultMatchers.jsonPath("$.message").value(message);
	}

	public ResultMatcher message(Matcher<String> messageMatcher) {
		return MockMvcResultMatchers.jsonPath("$.message").value(messageMatcher);
	}

	public ErrorResponseResultMatchers.Link link(String linkPath) {
		return new ErrorResponseResultMatchers.Link(linkPath);
	}

	public ErrorResponseResultMatchers.Link selfRef() {
		return new ErrorResponseResultMatchers.Link("self.href");
	}


	public ResultMatcher timestamp(LocalDateTime timestamp) {
		return MockMvcResultMatchers.jsonPath("$.timestamp").value(matchAssertion((String matchedTimestamp) -> {
			assertThat(LocalDateTime.parse(matchedTimestamp)).isEqualTo(timestamp);
		}));
	}

	public ResultMatcher timestamp(Matcher<LocalDateTime> timestampMatcher) {
		return MockMvcResultMatchers.jsonPath("$.timestamp").value(matchAssertion((String matchedTimestamp) -> {
			assertThat(timestampMatcher.matches(LocalDateTime.parse(matchedTimestamp))).isTrue();
		}));
	}

	public ResultMatcher validTimestamp() {
		return MockMvcResultMatchers.jsonPath("$.timestamp").value(matchAssertion((String timestamp) -> {
			LocalDateTime.parse(timestamp);
		}));
	}



	public static class Link {

		private final String path;

		private Link(String path) {
			this.path = "$._links." + path;
		}


		public ResultMatcher value(String link) {
			return MockMvcResultMatchers.jsonPath(path).value(link);
		}

		public ResultMatcher value(Matcher<String> linkMatcher) {
			return MockMvcResultMatchers.jsonPath(path).value(linkMatcher);
		}
	}

}
