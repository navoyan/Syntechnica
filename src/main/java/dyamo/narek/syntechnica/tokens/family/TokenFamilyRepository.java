package dyamo.narek.syntechnica.tokens.family;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenFamilyRepository extends JpaRepository<TokenFamily, Long> {
}
