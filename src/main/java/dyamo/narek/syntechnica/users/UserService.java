package dyamo.narek.syntechnica.users;

import dyamo.narek.syntechnica.users.authorities.UserAuthority;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityService;
import dyamo.narek.syntechnica.users.authorities.UserAuthorityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

	private final UserAuthorityService userAuthorityService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;


	public Optional<User> findUserByName(@NonNull String username) {
		return userRepository.findByName(username);
	}

	public boolean isUserMatchingPassword(@NonNull User user, @NonNull String rawPassword) {
		return passwordEncoder.matches(rawPassword, user.getPassword());
	}

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void saveDefaultAdminUserIfNotExists() {
		UserAuthority adminAuthority = userAuthorityService.findByType(UserAuthorityType.ADMIN)
				.stream().min(Comparator.comparingInt(UserAuthority::getId))
				.orElseThrow(() -> new IllegalStateException("Default admin authority not found"));

		if (userRepository.countByAuthorityId(adminAuthority.getId()) > 0) return;

		User admin = User.builder()
				.name("admin")
				.password(passwordEncoder.encode("admin"))
				.authorities(Collections.singletonList(adminAuthority))
				.build();

		adminAuthority.getUsers().add(admin);

		userRepository.save(admin);

		log.info("Created default admin user");
	}

}
