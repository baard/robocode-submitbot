package no.rehn.submitbot;

import java.util.Collection;

/**
 * Handles logins.
 */
public interface UserService {
	Collection<String> getUsers();
	boolean isValid(String username, String password);
}
