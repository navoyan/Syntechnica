package dyamo.narek.syntechnica.tokens.refresh;

import dyamo.narek.syntechnica.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	@Query("SELECT MAX(token.family) FROM RefreshToken token" +
			" WHERE token.user = :user")
	Optional<Long> findLastTokenFamilyOfUser(@Param("user") User user);


	@Query("SELECT token FROM RefreshToken token" +
			" WHERE token.user = :user AND token.family = :family" +
			" ORDER BY token.creationTimestamp DESC" +
			" LIMIT 1")
	Optional<RefreshToken> findLastTokenOfUserTokenFamily(@Param("user") User user, @Param("family") long family);


	@Modifying
	@Query("DELETE FROM RefreshToken token" +
			" WHERE token.user = :user AND token.family = :family")
	void deleteAllTokensOfUserTokenFamily(@Param("user") User user, @Param("family") long family);

}
