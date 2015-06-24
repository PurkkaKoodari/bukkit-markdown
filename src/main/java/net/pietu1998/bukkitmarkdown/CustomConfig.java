package net.pietu1998.bukkitmarkdown;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomConfig {
	private BukkitMarkdown plugin;
	private FileConfiguration conf = null;
	private File file = null;
	private String fname = null;

	public CustomConfig(BukkitMarkdown plugin, String filename) {
		this.plugin = plugin;
		fname = filename;
	}

	public void reloadConfig() {
		// Create file object if not done
		if (file == null)
			file = new File(plugin.getDataFolder(), fname);
		// Load configuration
		conf = YamlConfiguration.loadConfiguration(file);
	}

	public FileConfiguration getConfig() {
		// Reload configuration if needed
		if (conf == null)
			reloadConfig();

		return conf;
	}

	public void saveConfig() {
		// Don't save if there is nothing to save
		if (conf == null || file == null)
			return;
		// Try saving or log error
		try {
			conf.save(file);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error saving configuration file '" + fname + "'!", e);
		}
	}
}
