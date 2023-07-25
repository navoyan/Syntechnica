package dyamo.narek.syntechnica.global;

import org.assertj.core.matcher.AssertionMatcher;

import java.util.function.Consumer;

public class AssertionMatchers {

	private AssertionMatchers() {}


	public static <T> AssertionMatcher<T> matchAssertion(Consumer<T> requirement) {
		return new AssertionMatcher<>() {
			@Override
			public void assertion(T actual) throws AssertionError {
				requirement.accept(actual);
			}
		};
	}

}
