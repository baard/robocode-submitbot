package no.rehn.submitbot;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dumps and compiles an uploaded RoboCode bot.
 */
class DumpAndCompileHandler implements UploadBotHandler {
	static final String DEFAULT_JAVAC = "/usr/bin/javac";

	private final Logger logger = Logger.getLogger(getClass().getName());

	private final File outputDir;
	private String javac = DEFAULT_JAVAC;
	private final String robocodeJar;

	private final ByteArrayOutputStream captureStream;
	private String fileName;
	private String packageName;
	private boolean updated;

	DumpAndCompileHandler(File outputDir, String robocodeJar) {
		captureStream = new ByteArrayOutputStream();
		this.outputDir = outputDir;
		this.robocodeJar = robocodeJar;
	}

	@Override
	public boolean wasUpdated() {
		return updated;
	}

	@Override
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public void setFilename(String filename) {
		this.fileName = filename;
	}

	@Override
	public OutputStream getCaptureStream() {
		return captureStream;
	}

	@Override
	public void process() throws Exception {
		backup();
		store();
		compile();
	}

	private byte[] getBytes() {
		return captureStream.toByteArray();
	}

	private void debug(String msg) {
		String fqn = packageName + "/" + fileName;
		logger.log(Level.INFO, fqn + ": " + msg);
	}

	private void backup() throws IOException {
		File file = new File(outputDir, UUID.randomUUID().toString());
		overwrite(file, getBytes());
		debug("backed up as: " + file);
	}

	private void compile() throws IOException {
		if (!fileName.endsWith(".java")) {
			throw new IllegalArgumentException("must end with .java");
		}
		String expectedPackage = "package " + packageName + ";";
		String actualPackage = parsePackageDeclaration();
		if (!expectedPackage.equals(actualPackage)) {
			throw new IllegalArgumentException(actualPackage + " != " + expectedPackage);
		}
		Runtime runtime = Runtime.getRuntime();
		try {
			String command = String.format("%s -cp %s %s", javac, robocodeJar,
					packageName + File.separatorChar + fileName);
			Process compiler = runtime.exec(command, null, outputDir);
			if (compiler.waitFor() != 0) {
				String error = readAsString(compiler.getErrorStream());
				throw new IllegalStateException("exit value was "
						+ compiler.exitValue() + ", output was:\n" + error);
			}
			debug("compiled");
		} catch (InterruptedException e) {
			debug("interrupted");
			Thread.currentThread().interrupt();
		}
	}

	private String parsePackageDeclaration() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(captureStream.toByteArray())));
		try {
			return reader.readLine();
		} finally {
			reader.close();
		}
	}

	private void store() throws IOException {
		File packageDir = new File(outputDir, packageName);
		if (!packageDir.isDirectory()) {
			if (packageDir.mkdirs()) {
				debug("created " + packageDir);
			} else {
				throw new IllegalStateException("unable to create directory: "
						+ packageDir);				
			}
		}
		File file = new File(packageDir, fileName);
		boolean updating = file.exists();
		if (updating) {
			debug("overwriting " + file.getName());
		}
		overwrite(file, getBytes());
		debug("stored " + getBytes().length + " bytes");
		updated = true;
	}

	void overwrite(File file, byte[] bytes) throws FileNotFoundException,
			IOException {
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file));
		try {
			out.write(bytes);
		} finally {
			out.close();
		}
	}

	private String readAsString(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		return sb.toString();
	}
	
	public void setJavac(String javac) {
		this.javac = javac;
	}
}