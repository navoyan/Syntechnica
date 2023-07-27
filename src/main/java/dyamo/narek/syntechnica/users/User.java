package dyamo.narek.syntechnica.users;

import dyamo.narek.syntechnica.security.SecurityConfiguration;
import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authorized_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private static final String USERNAME_PATTERN = "^[a-z0-9](?!.*--)[a-z0-9-]{1,23}[a-z0-9]$";


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Pattern(regexp = USERNAME_PATTERN)
	private String name;

	@NotNull
	@Pattern(regexp = SecurityConfiguration.BCRYPT_HASH_PATTERN)
	private String password;


	@ManyToMany
	@JoinTable(
			name = "authorized_user__user_authority",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id")
	)
	private List<UserAuthority> authorities = new ArrayList<>();

}
