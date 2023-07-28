package dyamo.narek.syntechnica.security.auth.tokens;

import org.springframework.lang.NonNull;

public interface AccessTokenVersionProvider {

	long getAccessTokenCurrentVersion(@NonNull String username);

}
