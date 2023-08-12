package dyamo.narek.syntechnica.tokens.access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenMetadataRepository extends JpaRepository<AccessTokenMetadata, Integer> {

	@Query("SELECT metadata FROM AccessTokenMetadata metadata JOIN metadata.user user" +
			" WHERE user.name = :username")
	Optional<AccessTokenMetadata> findByUsername(@Param("username") String username);

}
