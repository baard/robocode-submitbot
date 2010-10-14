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

	private final Table botStats = new Table("Submitted bots");
	private final Table teamStats = new Table("Bots per team");
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
	    botStats.addContainerProperty("teamName", String.class, null);
	    botStats.addContainerProperty("file", File.class, null);
	    botStats.addContainerProperty("age", Long.class, null);
		botStats.addContainerProperty("lastUpdated", Date.class, null);
		botStats.setColumnHeaders(new String[] { "Team", "Bot", "Age (in minutes)", "Last updated" });
		teamStats.addContainerProperty("teamName", String.class, null);
		teamStats.addContainerProperty("numberOfBots", Integer.class, null);
		teamStats.setColumnHeaders(new String[] { "Team", "Number of bots" });
	}
	
	private ComponentContainer initWidget() {
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(botStats);
		layout.addComponent(teamStats);
		layout.addComponent(reload);
		layout.addComponent(lastRefreshed);
		return layout;
	}

	private void onReload() {
		reloadBots();
		reloadTeams();
		lastRefreshed.setValue("Refreshed " + new Date(System.currentTimeMillis()));
	}

	private void reloadTeams() {
		teamStats.removeAllItems();
		for (String team : userService.getUsers()) {
			File teamFolder = new File(outputDir, team);
			Item item = teamStats.addItem(team);
			item.getItemProperty("teamName").setValue(team);
			item.getItemProperty("numberOfBots").setValue(getBotCount(teamFolder));
		}
	}

	int getBotCount(File teamFolder) {
		if (!teamFolder.canRead()) {
			return 0;
		} else {
			return teamFolder.list(new CompiledBotsFilter()).length;
		}
	}

	void reloadBots() {
		botStats.removeAllItems();
		for (String team : userService.getUsers()) {
			File teamFolder = new File(outputDir, team);
			if (!teamFolder.canRead()) {
				continue;
			}
			String[] bots = teamFolder.list(new CompiledBotsFilter());
			for (String bot : bots) {
				File botFile = new File(teamFolder, bot);
				Item item = botStats.addItem(botFile);
				item.getItemProperty("file").setValue(new File(bot));
				Date lastModified = new Date(botFile.lastModified());
				item.getItemProperty("lastUpdated").setValue(lastModified);
				long ageInMillis = System.currentTimeMillis()
						- lastModified.getTime();
				item.getItemProperty("age").setValue(ageInMillis / (1000 * 60));
				item.getItemProperty("teamName").setValue(team);
			}
		}
	}

	static class CompiledBotsFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".class");
		}
	}
}
