package dyamo.narek.syntechnica.users.authorities;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthorityService {

	private final UserAuthorityRepository userAuthorityRepository;

	public @NonNull List<UserAuthority> findByType(@NonNull UserAuthorityType authorityType) {
		return userAuthorityRepository.findByType(authorityType);
	}

}
