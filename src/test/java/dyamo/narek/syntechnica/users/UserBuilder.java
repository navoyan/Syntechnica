package dyamo.narek.syntechnica.users;

import dyamo.narek.syntechnica.users.authorities.UserAuthority;

import java.util.ArrayList;
import java.util.Arrays;

public class UserBuilder {

	private static int lastUserIndex = 0;
	public static final String VALID_ENCODED_PASSWORD = "$2a$10$/T7x6zq6EBOmw9Br3zNoIu263D5TITk1QxK39AYgwN75AMUSCjwxu";

	private final int userIndex = ++lastUserIndex;
	private User configurableUser = new User(null, "user" + userIndex, VALID_ENCODED_PASSWORD, new ArrayList<>());


	public static UserBuilder user() {
		return new UserBuilder();
	}

	public static void resetIndex() {
		lastUserIndex = 0;
	}


	public UserBuilder withId() {
		configurableUser.setId(userIndex);
		return this;
	}

	public UserBuilder withId(Integer id) {
		configurableUser.setId(id);
		return this;
	}

	public UserBuilder withName(String name) {
		configurableUser.setName(name);
		return this;
	}

	public UserBuilder withPassword(String password) {
		configurableUser.setPassword(password);
		return this;
	}

	public UserBuilder withAuthorities(UserAuthority... authorities) {
		configurableUser.getAuthorities().addAll(Arrays.asList(authorities));

		for (UserAuthority authority : authorities) {
			if (!authority.getUsers().contains(configurableUser)) {
				authority.getUsers().add(configurableUser);
			}
		}

		return this;
	}

	public User build() {
		User builtUser = configurableUser;
		configurableUser = null;
		return builtUser;
	}

}
