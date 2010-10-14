package no.rehn.submitbot;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * Upload widget.
 */
@SuppressWarnings("serial")
class UploadBotWidget extends Panel {
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final UserService userService;

	private final ListSelect teamSelect = new ListSelect(
			"Please select your team");
	private final TextField passwordInput = new TextField("Password");
	private final Button loginButton = new Button("Login");
	private final Upload uploadField = new Upload();
	private final Provider<UploadBotHandler> handlerProvider;
	private UploadBotHandler currentHandler;
	private String currentFilename;

	UploadBotWidget(UserService logins,
			Provider<UploadBotHandler> handlerProvider) {
		this.userService = logins;
		this.handlerProvider = handlerProvider;
		setupWidgets();
		setContent(initWidget());
	}

	private void setupWidgets() {
		IndexedContainer teamSource = new IndexedContainer(userService.getUsers());
		teamSelect.setContainerDataSource(teamSource);
		teamSelect.setRows(1);
		teamSelect.setRequired(true);
		passwordInput.setSecret(true);
		passwordInput.setRequired(true);
		uploadField.setReceiver(new Upload.Receiver() {
			@Override
			public OutputStream receiveUpload(String filename, String MIMEType) {
				currentFilename = filename;
				currentHandler = handlerProvider.get();
				currentHandler.setPackageName(getCurrentTeam());
				currentHandler.setFilename(filename);
				return currentHandler.getCaptureStream();
			}
		});
		uploadField.setVisible(false);
		uploadField.addListener(new Upload.FinishedListener() {
			@Override
			public void uploadFinished(FinishedEvent event) {
				onUploadFinished();
			}
		});
		loginButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (isValidLogin()) {
					onLoggedIn();
				} else {
					onLoginFailed();
				}
			}
		});
	}

	private boolean isValidLogin() {
		String team = (String) teamSelect.getValue();
		String password = (String) passwordInput.getValue();
		return userService.isValid(team, password);
	}

	private void onLoginFailed() {
		getWindow().showNotification("Login failed", Notification.TYPE_WARNING_MESSAGE);
	}

	private void onLoggedIn() {
		teamSelect.setEnabled(false);
		passwordInput.setVisible(false);
		loginButton.setEnabled(false);
		uploadField.setVisible(true);
	}

	private void onUploadFinished() {
		try {
			currentHandler.process();
			notifyUser(processedMessage(), Notification.TYPE_HUMANIZED_MESSAGE);
		} catch (IllegalArgumentException e) {
			logger.log(Level.WARNING, logFormat("validation failed"), e);
			notifyUser(e.getMessage(), Notification.TYPE_WARNING_MESSAGE);
		} catch (Exception e) {
			logger.log(Level.SEVERE, logFormat("unexpected error"), e);
			notifyUser(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
		}
	}

	String processedMessage() {
		if (currentHandler.wasUpdated()) {
			return "has been updated";
		} else {
			return "new bot uploaded";
		}
	}

	private String logFormat(String message) {
		return String.format("File %s from %s: %s", currentFilename,
				getCurrentTeam(), message);
	}

	private void notifyUser(String message, int notificationType) {
		String prefixedMessage = currentFilename + ": " + message;
		getWindow().showNotification(prefixedMessage, notificationType);
	}

	private String getCurrentTeam() {
		return (String) teamSelect.getValue();
	}

	private ComponentContainer initWidget() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.addComponent(teamSelect);
		layout.addComponent(passwordInput);
		layout.addComponent(loginButton);
		layout.addComponent(uploadField);
		return layout;
	}
}
