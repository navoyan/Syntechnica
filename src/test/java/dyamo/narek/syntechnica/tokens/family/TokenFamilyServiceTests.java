package dyamo.narek.syntechnica.tokens.family;

import dyamo.narek.syntechnica.users.TestUserBuilder;
import dyamo.narek.syntechnica.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static dyamo.narek.syntechnica.users.TestUserBuilder.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenFamilyServiceTests {

	@Mock
	TokenFamilyRepository tokenFamilyRepository;

	@InjectMocks
	TokenFamilyService tokenFamilyService;


	@BeforeEach
	void beforeEach() {
		TestUserBuilder.resetIndex();
	}


	@Test
	void createTokenFamily_shouldCreateAndSaveValidTokenFamilyForSpecifiedUser() {
		User user = user().withId().build();

		given(tokenFamilyRepository.save(any(TokenFamily.class))).will(returnsFirstArg());


		TokenFamily createdTokenFamily = tokenFamilyService.createTokenFamily(user);


		verify(tokenFamilyRepository).save(createdTokenFamily);

		assertThat(createdTokenFamily.getUser()).isEqualTo(user);
		assertThat(createdTokenFamily.getLastGeneration()).isEqualTo(1L);
	}


	@Test
	void invalidateTokenFamily_shouldDeleteSpecifiedTokenFamily() {
		var family = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.lastGeneration(1L)
				.build();


		tokenFamilyService.invalidateTokenFamily(family);


		verify(tokenFamilyRepository).delete(family);
	}

	@Test
	void invalidateTokenFamily_shouldDeleteTokenFamilyWithSpecifiedId() {
		long familyId = 1L;


		tokenFamilyService.invalidateTokenFamily(familyId);


		verify(tokenFamilyRepository).deleteById(familyId);
	}


	@Test
	void getTokenFamilyLastGeneration_shouldReturnLastGenerationOptionalOfTokenFamilyWithSpecifiedId_whenFamilyExists() {
		long familyId = 5L;

		var persistedFamily = TokenFamily.builder()
				.id(familyId)
				.user(user().withId().build())
				.lastGeneration(2L)
				.build();

		given(tokenFamilyRepository.findById(familyId)).willReturn(Optional.of(persistedFamily));


		Optional<Long> lastGenerationOptional = tokenFamilyService.getTokenFamilyLastGeneration(familyId);


		assertThat(lastGenerationOptional).hasValue(persistedFamily.getLastGeneration());
	}

	@Test
	void getTokenFamilyLastGeneration_shouldReturnEmptyOptional_whenFamilyDoesntExist() {
		long familyId = 5L;

		given(tokenFamilyRepository.findById(familyId)).willReturn(Optional.empty());


		Optional<Long> lastGenerationOptional = tokenFamilyService.getTokenFamilyLastGeneration(familyId);


		assertThat(lastGenerationOptional).isEmpty();
	}


	@Test
	void updateTokenFamilyLastGeneration_shouldIncrementFamilyLastGenerationAndReturnUpdatedValue() {
		long initialLastGeneration = 2L;

		var family = TokenFamily.builder()
				.id(1L)
				.user(user().withId().build())
				.lastGeneration(initialLastGeneration)
				.build();


		long updatedLastGeneration = tokenFamilyService.updateTokenFamilyLastGeneration(family);


		assertThat(updatedLastGeneration).isEqualTo(initialLastGeneration + 1);
		assertThat(family.getLastGeneration()).isEqualTo(updatedLastGeneration);
	}

}