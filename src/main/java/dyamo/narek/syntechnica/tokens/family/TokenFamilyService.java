package dyamo.narek.syntechnica.tokens.family;

import dyamo.narek.syntechnica.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenFamilyService implements TokenGenerationValidityCoordinator {

	private final TokenFamilyRepository tokenFamilyRepository;


	public TokenFamily createTokenFamily(@NonNull @Valid User user) {
		var newFamily = TokenFamily.builder()
				.user(user)
				.lastGeneration(1)
				.build();

		return tokenFamilyRepository.save(newFamily);
	}


	@CacheEvict(key = "#family.id", cacheNames = "familyLastGenerations", cacheManager = "tokenMetadataCacheManager")
	public void invalidateTokenFamily(@NonNull TokenFamily family) {
		tokenFamilyRepository.delete(family);
	}

	@CacheEvict(cacheNames = "familyLastGenerations", cacheManager = "tokenMetadataCacheManager")
	@Override
	public void invalidateTokenFamily(long familyId) {
		tokenFamilyRepository.deleteById(familyId);
	}


	@Cacheable(cacheNames = "familyLastGenerations", cacheManager = "tokenMetadataCacheManager")
	@Override
	public Optional<Long> getTokenFamilyLastGeneration(long familyId) {
		return tokenFamilyRepository.findById(familyId)
				.map(TokenFamily::getLastGeneration);
	}


	@CachePut(key = "#family.id", cacheNames = "familyLastGenerations", cacheManager = "tokenMetadataCacheManager")
	public long updateTokenFamilyLastGeneration(@NonNull TokenFamily family) {
		long newLastGeneration = family.getLastGeneration() + 1;
		family.setLastGeneration(newLastGeneration);

		return newLastGeneration;
	}

}
