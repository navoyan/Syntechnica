package dyamo.narek.syntechnica.users;

import dyamo.narek.syntechnica.users.authorities.UserAuthority;

import java.util.ArrayList;
import java.util.Arrays;

public class TestUserBuilder {

	private static int lastUserIndex = 0;
	public static final String VALID_ENCODED_PASSWORD = "$2a$10$/T7x6zq6EBOmw9Br3zNoIu263D5TITk1QxK39AYgwN75AMUSCjwxu";

	private final int userIndex = ++lastUserIndex;
	private User configurableUser = User.builder()
			.name("user" + userIndex)
			.password(VALID_ENCODED_PASSWORD)
			.authorities(new ArrayList<>())
			.tokenFamilies(new ArrayList<>())
			.build();


	public static TestUserBuilder user() {
		return new TestUserBuilder();
	}

	public static void resetIndex() {
		lastUserIndex = 0;
	}


	public TestUserBuilder withId() {
		configurableUser.setId(userIndex);
		return this;
	}

	public TestUserBuilder withId(Integer id) {
		configurableUser.setId(id);
		return this;
	}

	public TestUserBuilder withName(String name) {
		configurableUser.setName(name);
		return this;
	}

	public TestUserBuilder withPassword(String password) {
		configurableUser.setPassword(password);
		return this;
	}

	public TestUserBuilder withAuthorities(UserAuthority... authorities) {
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
