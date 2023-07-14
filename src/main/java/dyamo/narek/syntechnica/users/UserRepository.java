package dyamo.narek.syntechnica.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByName(@NonNull String username);

	@Query("SELECT COUNT(user) FROM User user JOIN user.authorities authority" +
			" WHERE authority.id = :authorityId")
	int countByAuthorityId(@Param("authorityId") int authorityId);

}
