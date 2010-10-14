package no.rehn.submitbot;

import java.io.File;

import com.vaadin.Application;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

/**
 * Application wiring.
 */
@SuppressWarnings("serial")
public class SubmitBot extends Application {
	final String robocodeJar = "/home/baard/robocode.jar";
	final File outputDir = new File("/home/baard/jz09");
	final String loginFile = "/home/baard/robo-uploaders.properties";

	@Override
	public void init() {
		UserService logins = new FileBasedUserService(loginFile);
		Provider<UploadBotHandler> handlerProvider = createUploadHandler();
		UploadBotWidget uploadBotWidget = new UploadBotWidget(logins,
				handlerProvider);
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(uploadBotWidget);
		layout.addComponent(new BotStatusWidget(logins, outputDir));
		setMainWindow(new Window("Submit your bot", layout));
	}

	Provider<UploadBotHandler> createUploadHandler() {
		return new Provider<UploadBotHandler>() {
			@Override
			public UploadBotHandler get() {
				return new DumpAndCompileHandler(outputDir, robocodeJar);
			}
		};
	}
}
