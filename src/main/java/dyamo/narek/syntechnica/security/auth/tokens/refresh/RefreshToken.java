package dyamo.narek.syntechnica.security.auth.tokens.refresh;

import dyamo.narek.syntechnica.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

	@Id
	@NotNull
	private UUID value;


	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	@NotNull
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private User user;


	private long family;

	@NotNull
	private Instant creationTimestamp;

	@NotNull
	private Instant expirationTimestamp;

}
