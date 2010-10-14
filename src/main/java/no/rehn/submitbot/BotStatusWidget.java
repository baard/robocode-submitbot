package no.rehn.submitbot;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * List bots for each team.
 */
@SuppressWarnings("serial")
class BotStatusWidget extends Panel {
	final UserService userService;
	final File outputDir;

	private final Table table = new Table("Submitted bots");
	private final Button reload = new Button("Reload");
	private final Label lastRefreshed = new Label("Last refreshed");

	BotStatusWidget(UserService userService, File outputDir) {
		this.userService = userService;
		this.outputDir = outputDir;
		setupWidget();
		setContent(initWidget());
		onReload();
	}

	private void setupWidget() {
		reload.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				onReload();
			}
		});
	    table.addContainerProperty("file", File.class, null);
		table.addContainerProperty("lastUpdated", Date.class, null);
	    table.addContainerProperty("teamName", String.class, null);
	    table.addContainerProperty("age", Long.class, null);
		table.setColumnHeaders(new String[] { "teamName", "file", "lastUpdated", "ageInMinutes" });
	}
	
	private ComponentContainer initWidget() {
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(table);
		layout.addComponent(reload);
		layout.addComponent(lastRefreshed);
		return layout;
	}

	private void onReload() {
		table.removeAllItems();
		for (String team : userService.getUsers()) {
			File teamFolder = new File(outputDir, team);
			String[] bots = teamFolder.list(new CompiledBotsFilter());
			for (String bot : bots) {
				File botFile = new File(teamFolder, bot);
				Item botItem = table.addItem(botFile);
				botItem.getItemProperty("file").setValue(new File(bot));
				Date lastModified = new Date(botFile.lastModified());
				botItem.getItemProperty("lastUpdated").setValue(lastModified);
				long ageInMillis = System.currentTimeMillis()
						- lastModified.getTime();
				botItem.getItemProperty("age").setValue(ageInMillis / (1000 * 60));
				botItem.getItemProperty("teamName").setValue(team);
			}
		}
		lastRefreshed.setValue("Refreshed " + new Date(System.currentTimeMillis()));
	}

	static class CompiledBotsFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".class");
		}
	}
}
