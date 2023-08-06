package dyamo.narek.syntechnica.global.errors;

import org.hamcrest.Matcher;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
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


	public JsonPathResultMatchers message() {
		return MockMvcResultMatchers.jsonPath("$.message");
	}


	public JsonPathResultMatchers path() {
		return MockMvcResultMatchers.jsonPath("$.path");
	}


	public JsonPathResultMatchers timestamp() {
		return MockMvcResultMatchers.jsonPath("$.timestamp");
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

}
