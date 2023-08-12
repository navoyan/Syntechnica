package dyamo.narek.syntechnica.tokens.family;

import java.util.Optional;

public interface TokenGenerationValidityCoordinator {

	Optional<Long> getTokenFamilyLastGeneration(long familyId);

	void invalidateTokenFamily(long familyId);

}
