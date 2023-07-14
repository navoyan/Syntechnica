package dyamo.narek.syntechnica.users.authorities;

import dyamo.narek.syntechnica.users.User;

import java.util.Arrays;

public class UserAuthorityBuilder {

	private static int lastAuthorityIndex = 0;

	private final int authorityIndex = ++lastAuthorityIndex;
	private UserAuthority configurableAuthority = new UserAuthority();


	public static UserAuthorityBuilder authority() {
		return new UserAuthorityBuilder();
	}

	public static void resetIndex() {
		lastAuthorityIndex = 0;
	}


	public UserAuthorityBuilder withId() {
		configurableAuthority.setId(authorityIndex);
		return this;
	}

	public UserAuthorityBuilder withId(Integer id) {
		configurableAuthority.setId(id);
		return this;
	}

	public UserAuthorityBuilder withType(UserAuthorityType type) {
		configurableAuthority.setType(type);
		return this;
	}

	public UserAuthorityBuilder withScope(String scope) {
		configurableAuthority.setScope(scope);
		return this;
	}

	public UserAuthorityBuilder withUsers(User... users) {
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
