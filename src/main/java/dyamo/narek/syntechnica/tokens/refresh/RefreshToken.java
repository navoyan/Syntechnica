package dyamo.narek.syntechnica.tokens.refresh;

import dyamo.narek.syntechnica.tokens.family.TokenFamily;
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
	@JoinColumn(name = "family_id")
	@NotNull
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private TokenFamily family;


	private long generation;

	@NotNull
	private Instant creationTimestamp;

	@NotNull
	private Instant expirationTimestamp;



	public boolean isExpired() {
		return expirationTimestamp.isBefore(Instant.now());
	}

	public boolean isLastGeneration() {
		return generation == family.getLastGeneration();
	}

}
