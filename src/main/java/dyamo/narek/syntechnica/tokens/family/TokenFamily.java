package dyamo.narek.syntechnica.tokens.family;

import dyamo.narek.syntechnica.tokens.refresh.RefreshToken;
import dyamo.narek.syntechnica.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "token_family")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenFamily {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private User user;


	private long lastGeneration;


	@OneToMany(mappedBy = "family")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private List<RefreshToken> refreshTokens = new ArrayList<>();


}
