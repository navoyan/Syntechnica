package dyamo.narek.syntechnica.users.authorities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Integer> {

	@NonNull List<UserAuthority> findByType(@NonNull UserAuthorityType type);

}
