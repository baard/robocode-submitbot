package no.rehn.submitbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Loads usernames and password from a file.
 */
class FileBasedUserService implements UserService {
	final Properties props;

	FileBasedUserService(String filename) {
		try {
			this.props = loadProperties(filename);
		} catch (IOException e) {
			String message = "Unable to load logins: " + filename;
			throw new IllegalArgumentException(message, e);
		}
	}

	Properties loadProperties(String filename) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(filename));
		return props;
	}

	@Override
	public Collection<String> getUsers() {
		ArrayList<String> list = new ArrayList<String>(props.stringPropertyNames());
		Collections.sort(list);
		return list;
	}

	@Override
	public boolean isValid(String username, String password) {
		if (username == null) {
			return false;
		}
		String existing = props.getProperty(username);
		return existing != null && existing.equals(password);
	}
}