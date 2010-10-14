package no.rehn.submitbot;

import java.io.OutputStream;

/**
 * Upload handler.
 */
interface UploadBotHandler {
	OutputStream getCaptureStream();

	void setPackageName(String teamName);

	void setFilename(String filename);

	void process() throws Exception;

	boolean wasUpdated();
}