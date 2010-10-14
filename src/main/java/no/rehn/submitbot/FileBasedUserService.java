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
	private final String filename;

	FileBasedUserService(String filename) {
		this.filename = filename;
	}

	Properties doLoadProperties() throws IOException {
		Properties props = new Properties();
		FileInputStream in = new FileInputStream(filename);
		try {
			props.load(in);
		} finally {
			in.close();
		}
		return props;
	}

	@Override
	public Collection<String> getUsers() {
		ArrayList<String> list = new ArrayList<String>(loadProperties().stringPropertyNames());
		Collections.sort(list);
		return list;
	}

	private Properties loadProperties() {
		try {
			return doLoadProperties();
		} catch (IOException e) {
			String message = "Unable to load logins: " + filename;
			throw new IllegalArgumentException(message, e);
		}
	}

	@Override
	public boolean isValid(String username, String password) {
		if (username == null) {
			return false;
		}
		String existing = loadProperties().getProperty(username);
		return existing != null && existing.equals(password);
	}
}