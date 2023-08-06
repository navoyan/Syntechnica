package dyamo.narek.syntechnica.tokens.access;

import dyamo.narek.syntechnica.users.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "access_token_metadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessTokenMetadata {

	@Id
	private int userId;

	@OneToOne(optional = false)
	@PrimaryKeyJoinColumn
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private User user;


	private long version;

}
