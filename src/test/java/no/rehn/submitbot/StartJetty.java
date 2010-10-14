package no.rehn.submitbot;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Launches the application in a web container.
 */
public class StartJetty {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		server.addHandler(new WebAppContext("src/main/webapp", "/submitbot"));
		server.start();
	}
}
