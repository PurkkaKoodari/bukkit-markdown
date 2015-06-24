package net.pietu1998.bukkitmarkdown;

import org.bukkit.ChatColor;

public class Messages {

	// Help message for players in chat

	static final String[] MARKDOWN_HELP = new String[] {
			ChatColor.YELLOW + "--------" + ChatColor.WHITE + " Available Markdown formatting " + ChatColor.YELLOW
					+ "--------",
			ChatColor.GREEN + "__" + ChatColor.WHITE + ChatColor.BOLD + "bold" + ChatColor.GREEN + "__ **"
					+ ChatColor.WHITE + ChatColor.BOLD + "bold" + ChatColor.GREEN + "** <b>" + ChatColor.WHITE
					+ ChatColor.BOLD + "bold" + ChatColor.GREEN + "</b>",
			ChatColor.GREEN + "_" + ChatColor.WHITE + ChatColor.ITALIC + "italic" + ChatColor.GREEN + "_ *"
					+ ChatColor.WHITE + ChatColor.ITALIC + "italic" + ChatColor.GREEN + "* <i>" + ChatColor.WHITE
					+ ChatColor.ITALIC + "italic" + ChatColor.GREEN + "</i>",
			ChatColor.GREEN + "<u>" + ChatColor.WHITE + ChatColor.UNDERLINE + "underline" + ChatColor.GREEN + "</u>",
			ChatColor.GREEN + "<s>" + ChatColor.WHITE + ChatColor.STRIKETHROUGH + "strikethrough" + ChatColor.GREEN
					+ "</s> <del>" + ChatColor.WHITE + ChatColor.STRIKETHROUGH + "strikethrough" + ChatColor.GREEN
					+ "</del>" };

	// Help message for console, etc

	static final String[] MARKDOWN_HELP_CONSOLE = new String[] { "Available Markdown formatting:", "__bold__",
			"**bold**", "<b>bold/b>", "_italic_", "*italic*", "<i>italic</i>", "<u>underline</u>",
			"<s>strikethrough</s>", "<del>strikethrough</del>" };

	// Markdown status messages

	static final String ENABLED = ChatColor.GREEN + "enabled" + ChatColor.RESET;
	static final String DISABLED = ChatColor.RED + "disabled" + ChatColor.RESET;
	static final String DISALLOWED = ChatColor.RED + "not allowed" + ChatColor.RESET;

	static final String MD_TOGGLED = "Markdown in {0} is now {1} for {2}.";

	static final String MD_STATUS = "Markdown in {0} is currently {1} for {2}.";

	static final String MD_DISALLOWED_SELF = "You are " + DISALLOWED + " to use Markdown in {0}.";
	static final String MD_DISALLOWED_OTHER = "{0} is " + DISALLOWED + " to use Markdown in {1}.";

	// Error messages

	static final String NO_PERMISSION = ChatColor.DARK_RED + "You don't have permission to do that.";
	static final String NOT_ONLINE = ChatColor.DARK_RED + "That player is not currently online.";
	static final String MUST_BE_PLAYER = "Only players can use this command.";

	// Reload message
	static final String RELOADED = "Configuration reloaded.";

	private Messages() {}

}
