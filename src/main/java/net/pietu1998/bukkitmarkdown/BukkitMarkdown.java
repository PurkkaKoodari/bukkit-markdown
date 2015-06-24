package net.pietu1998.bukkitmarkdown;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMarkdown extends JavaPlugin {
	private CustomConfig playerData = null;

	@Override
	public void onEnable() {
		// Get configuration files
		playerData = new CustomConfig(this, "players.yml");
		FileConfiguration config = getConfig();
		// Set default configuration
		config.addDefault("onByDefault.chat", true);
		config.addDefault("onByDefault.signs", false);
		config.addDefault("statusInMotd.chat.enabled", true);
		config.addDefault("statusInMotd.chat.messages.enabled",
				MessageFormat.format(Messages.MD_STATUS, "chat", Messages.ENABLED, "you"));
		config.addDefault("statusInMotd.chat.messages.disabled",
				MessageFormat.format(Messages.MD_STATUS, "chat", Messages.DISABLED, "you"));
		config.addDefault("statusInMotd.signs.enabled", true);
		config.addDefault("statusInMotd.signs.messages.enabled",
				MessageFormat.format(Messages.MD_STATUS, "signs", Messages.ENABLED, "you"));
		config.addDefault("statusInMotd.signs.messages.disabled",
				MessageFormat.format(Messages.MD_STATUS, "signs", Messages.DISABLED, "you"));
		config.addDefault("informPlayersOfChangeByOp", true);
		config.addDefault("containDisallowedFeaturesInStatus", true);
		// Save defaults to the file
		config.options().copyDefaults(true);
		saveConfig();
		// Register listener for events
		getServer().getPluginManager().registerEvents(new PlayerMarkdownListener(this), this);
	}

	@Override
	public void onDisable() {}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		// Reload player data
		if (playerData != null)
			playerData.reloadConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		FileConfiguration config = getConfig();
		// Markdown command, currently the only one
		if (command.getName().equalsIgnoreCase("markdown"))
			return markdownCommand(sender, command, label, args);

		return false;
	}

	/**
	 * Processes the /markdown command.
	 */
	private boolean markdownCommand(CommandSender sender, Command command, String label, String[] args) {
		// Help command, print available MD
		if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			// Check permissions
			if (!sender.hasPermission("bukkitmarkdown.help")) {
				sender.sendMessage(Messages.NO_PERMISSION);
				return true;
			}
			// Send correct help message
			if (sender instanceof Player)
				sender.sendMessage(Messages.MARKDOWN_HELP);
			else
				sender.sendMessage(Messages.MARKDOWN_HELP_CONSOLE);
			return true;
		}
		// Reload command
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			// Check permissions
			if (!sender.hasPermission("bukkitmarkdown.reload")) {
				sender.sendMessage(Messages.NO_PERMISSION);
				return true;
			}
			// Reload
			reloadConfig();
			// Send confirmation
			sender.sendMessage(Messages.RELOADED);
			return true;
		}
		// Toggle command with no arguments, toggle for self
		if (args.length == 1 && (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("signs"))) {
			// Only toggleable for a player
			if (!(sender instanceof Player)) {
				sender.sendMessage(Messages.MUST_BE_PLAYER);
				return true;
			}
			// Check permissions
			if (!sender.hasPermission("bukkitmarkdown.toggle" + args[0])) {
				sender.sendMessage(Messages.NO_PERMISSION);
				return true;
			}
			// Do toggling to the player
			toggleMarkdown(sender, (Player) sender, args[0]);
			return true;
		}
		// Toggle command with player argument
		if (args.length == 2 && (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("signs"))) {
			// Check permissions
			if (!sender.hasPermission("bukkitmarkdown.toggleany")) {
				sender.sendMessage(Messages.NO_PERMISSION);
				return true;
			}
			// Find target player
			// Using the deprecated getPlayer(String) because we get a name as
			// argument, not a UUID. We then check if the result is null and use
			// UUIDs everywhere else.
			Player target = Bukkit.getServer().getPlayer(args[1]);
			// Check that the player exists
			if (target == null || !target.isOnline()) {
				sender.sendMessage(Messages.NOT_ONLINE);
				return true;
			}
			// Do toggling
			toggleMarkdown(sender, target, args[0]);
			return true;
		}
		// Status command with no arguments, check for self
		if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
			// Only usable for a player
			if (!(sender instanceof Player)) {
				sender.sendMessage(Messages.MUST_BE_PLAYER);
				return true;
			}
			// Check permissions
			if (!sender.hasPermission("bukkitmarkdown.status")) {
				sender.sendMessage(Messages.NO_PERMISSION);
				return true;
			}
			// Return the status for the player
			sendMarkdownStatus(sender, (Player) sender);
			return true;
		}
		// Toggle command with player argument
		if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
			// Check permissions
			if (!sender.hasPermission("bukkitmarkdown.statusany")) {
				sender.sendMessage(Messages.NO_PERMISSION);
				return true;
			}
			// Find target player
			// Using the deprecated getPlayer(String) because we get a name as
			// argument, not a UUID. We then check if the result is null and use
			// UUIDs everywhere else.
			Player target = Bukkit.getServer().getPlayer(args[1]);
			// Check that the player exists
			if (target == null || !target.isOnline()) {
				sender.sendMessage(Messages.NOT_ONLINE);
				return true;
			}
			// Return the status
			sendMarkdownStatus(sender, target);
			return true;
		}
		return false;
	}

	/**
	 * Toggles Markdown for a user and tells users about it.
	 * 
	 * @param sender
	 *            the user performing the command.
	 * @param target
	 *            the user to toggle Markdown on.
	 * @param type
	 *            the type of Markdown to toggle (must be one of "chat" or
	 *            "signs")
	 */
	private void toggleMarkdown(CommandSender sender, Player target, String type) {
		// Get current setting and reverse
		boolean enabled = !isMarkdownEnabled(target.getUniqueId(), type);
		// Update setting
		setMarkdownEnabled(target.getUniqueId(), type, enabled);
		// Check for self change
		if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
			// Send toggle message
			sendToggleMessage(sender, type, enabled, "you");
		} else {
			// Send toggle message to sender
			sendToggleMessage(sender, type, enabled, target.getDisplayName());
			// Send optional message to target
			if (getConfig().getBoolean("informPlayersOfChangeByOp"))
				sendToggleMessage(target, type, enabled, "you");
		}
	}

	/**
	 * Sends a message to a user telling that Markdown has been toggled,
	 * 
	 * @param to
	 *            the user to send the message to
	 * @param type
	 *            the type of Markdown that was toggled (must be one of "chat"
	 *            or "signs")
	 * @param enabled
	 *            whether or not Markdown was enabled
	 * @param name
	 *            the name of the user that will be put in the message
	 */
	private void sendToggleMessage(CommandSender to, String type, boolean enabled, String name) {
		// Send message to target, with details formatted in
		to.sendMessage(MessageFormat.format(Messages.MD_TOGGLED, type, enabled ? Messages.ENABLED : Messages.DISABLED,
				name));
	}

	/**
	 * Sends a player's Markdown status.
	 * 
	 * @param sender
	 *            the player performing the command
	 * @param target
	 *            the player to check the status on
	 */
	private void sendMarkdownStatus(CommandSender sender, Player target) {
		// Get player preferences
		boolean chatEnabled = isMarkdownEnabled(target.getUniqueId(), "chat");
		// Check for self
		String nameToShow = target.getDisplayName();
		boolean self = sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId());
		if (self)
			nameToShow = "you";
		sendMarkdownStatusByType(sender, target, "chat", self, nameToShow);
		sendMarkdownStatusByType(sender, target, "signs", self, nameToShow);
	}

	private void sendMarkdownStatusByType(CommandSender sender, Player target, String type, boolean self, String nameToShow) {
		FileConfiguration config = getConfig();
		// Get player preferences
		boolean enabled = isMarkdownEnabled(target.getUniqueId(), type);
		// If allowed to use, send status
		if (target.hasPermission("bukkitmarkdown.use" + type)) {
			sender.sendMessage(MessageFormat.format(Messages.MD_STATUS, type, enabled ? Messages.ENABLED
					: Messages.DISABLED, nameToShow));
		} else if (config.getBoolean("containDisallowedFeaturesInStatus")) {
			// Send disallowed message
			if (self)
				sender.sendMessage(MessageFormat.format(Messages.MD_DISALLOWED_SELF, type));
			else
				sender.sendMessage(MessageFormat.format(Messages.MD_DISALLOWED_OTHER, nameToShow, type));
		}
	}

	/**
	 * Checks if a player is using Markdown currently.
	 * 
	 * @param playerId
	 *            the UUID of the player
	 * @param type
	 *            the type of Markdown to be checked (must be one of "chat" or
	 *            "signs")
	 * @return if the given type of Markdown is enabled on that player
	 */
	public boolean isMarkdownEnabled(UUID playerId, String type) {
		// If the player is not permitted to use Markdown, then it's not enabled
		if (!Bukkit.getServer().getPlayer(playerId).hasPermission("bukkitmarkdown.use" + type))
			return false;
		// Check if Markdown is on by default
		boolean defaultOn = getConfig().getBoolean("onByDefault." + type);
		// Get player preferences
		return playerData.getConfig().getBoolean("players." + playerId.toString() + "." + type, defaultOn);
	}

	/**
	 * Sets a player's Markdown preference.
	 * 
	 * @param playerId
	 *            the UUID of the player
	 * @param type
	 *            the type of Markdown to be set
	 * @param enabled
	 *            whether or not Markdown should be enabled on that player
	 */
	private void setMarkdownEnabled(UUID playerId, String type, boolean enabled) {
		// Set player preferences and save
		playerData.getConfig().set("players." + playerId.toString() + "." + type, enabled);
		playerData.saveConfig();
	}

}
