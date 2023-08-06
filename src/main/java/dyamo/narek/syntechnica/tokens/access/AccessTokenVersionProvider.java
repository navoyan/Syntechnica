package dyamo.narek.syntechnica.tokens.access;

import org.springframework.lang.NonNull;

public interface AccessTokenVersionProvider {

	long getAccessTokenCurrentVersion(@NonNull String username);

}
