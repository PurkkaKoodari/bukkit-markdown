package net.pietu1998.bukkitmarkdown;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerMarkdownListener implements Listener {

	private BukkitMarkdown plugin;

	public PlayerMarkdownListener(BukkitMarkdown plugin) {
		this.plugin = plugin;
	}

	/**
	 * Called when a player sends a message in chat.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void chatMessageSent(AsyncPlayerChatEvent event) {
		// Check if the message should be affected by the plugin
		if (plugin.isMarkdownEnabled(event.getPlayer().getUniqueId(), "chat")) {
			// Calculate the full message based on the format given to us
			String fullMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(),
					event.getMessage());
			// Get the last color in the message to find out the color to reset
			// to after an ending tag
			String lastColor = ChatColor.getLastColors(fullMessage);
			// Parse the message and change it in the event object
			event.setMessage(MarkdownParser.markdownToChat(event.getMessage(), lastColor));
		}
	}

	/**
	 * Called when a player modifies a sign.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void signTextSet(SignChangeEvent event) {
		// Check if the operation should be affected by the plugin
		if (plugin.isMarkdownEnabled(event.getPlayer().getUniqueId(), "signs")) {
			// Parse each line and change them in the event object
			for (int i = 0; i < 4; i++)
				event.setLine(i, MarkdownParser.markdownToChat(event.getLine(i), ""));
		}
	}

	/**
	 * Called when a player logs in.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoined(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final FileConfiguration config = plugin.getConfig();
		// Get the player's settings
		final boolean chatEnabled = plugin.isMarkdownEnabled(player.getUniqueId(), "chat");
		final boolean signsEnabled = plugin.isMarkdownEnabled(player.getUniqueId(), "signs");
		// Task for sending the player their status
		class MotdStatusTask implements Runnable {
			@Override
			public void run() {
				// Check that the player is allowed to use Markdown in chat &
				// signs and the status MOTD is enabled for them, then send
				// messages corresponding to player preferences
				if (player.hasPermission("bukkitmarkdown.usechat") && config.getBoolean("statusInMotd.chat.enabled")) {
					if (chatEnabled)
						player.sendMessage(config.getString("statusInMotd.chat.messages.enabled"));
					else
						player.sendMessage(config.getString("statusInMotd.chat.messages.disabled"));
				}
				if (player.hasPermission("bukkitmarkdown.usesigns") && config.getBoolean("statusInMotd.signs.enabled")) {
					if (signsEnabled)
						player.sendMessage(config.getString("statusInMotd.signs.messages.enabled"));
					else
						player.sendMessage(config.getString("statusInMotd.signs.messages.disabled"));
				}
			}
		}
		// Send the task as delayed so any join messages etc. come first
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new MotdStatusTask());
	}
}
