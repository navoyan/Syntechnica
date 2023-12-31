package dyamo.narek.syntechnica.users.authorities;

import dyamo.narek.syntechnica.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthority implements GrantedAuthority {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private UserAuthorityType type;

	private String scope;


	@ManyToMany(mappedBy = "authorities")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private List<User> users = new ArrayList<>();



	@Transient
	@Override
	public String getAuthority() {
		if (scope != null) {
			return type + ":" + scope;
		}
		else {
			return type.toString();
		}
	}

}
