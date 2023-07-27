package dyamo.narek.syntechnica.users.authorities;

import dyamo.narek.syntechnica.users.User;

import java.util.Arrays;

public class TestUserAuthorityBuilder {

	private static int lastAuthorityIndex = 0;

	private final int authorityIndex = ++lastAuthorityIndex;
	private UserAuthority configurableAuthority = new UserAuthority();


	public static TestUserAuthorityBuilder authority() {
		return new TestUserAuthorityBuilder();
	}

	public static void resetIndex() {
		lastAuthorityIndex = 0;
	}


	public TestUserAuthorityBuilder withId() {
		configurableAuthority.setId(authorityIndex);
		return this;
	}

	public TestUserAuthorityBuilder withId(Integer id) {
		configurableAuthority.setId(id);
		return this;
	}

	public TestUserAuthorityBuilder withType(UserAuthorityType type) {
		configurableAuthority.setType(type);
		return this;
	}

	public TestUserAuthorityBuilder withScope(String scope) {
		configurableAuthority.setScope(scope);
		return this;
	}

	public TestUserAuthorityBuilder withUsers(User... users) {
		configurableAuthority.getUsers().addAll(Arrays.asList(users));

		for (User user : users) {
			if (!user.getAuthorities().contains(configurableAuthority)) {
				user.getAuthorities().add(configurableAuthority);
			}
		}

		return this;
	}

	public UserAuthority build() {
		UserAuthority builtAuthority = configurableAuthority;
		configurableAuthority = null;
		return builtAuthority;
	}

}
