package net.pietu1998.bukkitmarkdown;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class MarkdownParser {

	private MarkdownParser() {}

	/**
	 * Converts mixed Markdown + HTML to Minecraft chat.
	 * 
	 * @param message
	 *            the message in mixed Markdown + HTML format
	 * @param lastColor
	 *            the color string that specifies "default" format for the
	 *            message
	 * @return the message in Minecraft chat format
	 */
	public static String markdownToChat(String message, String lastColor) {
		// Convert mixed Markdown + HTML to HTML and then to chat
		return htmlToChat(markdownToHtml(message), lastColor);
	}

	/**
	 * Converts mixed Markdown + HTML to HTML.
	 * 
	 * @param markdown
	 *            the text in mixed Markdown + HTML format
	 * @return the text in HTML format
	 */
	public static String markdownToHtml(String markdown) {
		// Replace tildes and dollars by codes to make things easier
		markdown = markdown.replaceAll("~", "~T").replaceAll("\\$", "~D");

		// Encode special characters
		markdown = encodeCharacters(markdown, "\\\\(\\\\)");
		markdown = encodeCharacters(markdown, "\\\\([`*_{}\\[\\]()>#+-.!])");

		// Parse __bold__ and _italic_
		markdown = markdown.replaceAll("(\\*\\*|__)(?=\\S)(.*?\\S[*_]*)\\1", "<b>$2</b>");
		markdown = markdown.replaceAll("(\\w)_(\\w)", "$1~E95E$2");
		markdown = markdown.replaceAll("(\\*|_)(?=\\S)(.*?\\S)\\1", "<i>$2</i>");

		// Convert encoded characters back
		Matcher matcher = Pattern.compile("~E(\\d+)E").matcher(markdown);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			int charCodeToReplace = Integer.parseInt(matcher.group(1));
			matcher.appendReplacement(buffer, new String(new char[] { (char) charCodeToReplace }));
		}
		matcher.appendTail(buffer);
		markdown = buffer.toString();

		// Replace tildes and dollars back
		markdown = markdown.replaceAll("~D", "\\$").replaceAll("~T", "~");

		return markdown;
	}

	/**
	 * Encodes special characters with regex.
	 * 
	 * @param markdown
	 *            the text to be encoded
	 * @param regex
	 *            a regex matching all characters to encode
	 * @return the message with all regex matches encoded
	 */
	private static String encodeCharacters(String markdown, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(markdown);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String charCode = Integer.toString(matcher.group().charAt(0));
			matcher.appendReplacement(buffer, "~E" + charCode + "E");
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/**
	 * Converts HTML to Minecraft chat.
	 * 
	 * @param html
	 *            the text in HTML format
	 * @param lastColor
	 *            the color string that specifies "default" format for the text
	 * @return the text in Minecraft chat format
	 */
	public static String htmlToChat(String html, String color) {
		State state = new State();
		// Pattern to match the HTML tags
		Matcher matcher = Pattern.compile("<(/?)(b|i|u|s|del)>").matcher(html);
		// Replacement buffer
		StringBuffer buffer = new StringBuffer();
		// Find all tags
		while (matcher.find()) {
			// Check for starting/ending tag
			boolean start = matcher.group(1).isEmpty();
			// Determine the element type, set the styling flag according to the
			// starting/ending tag, and append any formatting changes
			switch (matcher.group(2)) {
			case "b":
				state.bold = start;
				matcher.appendReplacement(buffer, start ? ChatColor.BOLD.toString() : state.generateReset(color));
				break;
			case "i":
				state.italic = start;
				matcher.appendReplacement(buffer, start ? ChatColor.ITALIC.toString() : state.generateReset(color));
				break;
			case "u":
				state.underline = start;
				matcher.appendReplacement(buffer, start ? ChatColor.UNDERLINE.toString() : state.generateReset(color));
				break;
			case "s":
			case "del":
				state.strikethrough = start;
				matcher.appendReplacement(buffer,
						start ? ChatColor.STRIKETHROUGH.toString() : state.generateReset(color));
				break;
			}
		}
		// Append rest of match
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/**
	 * An object representing the current state in the HTML parser.
	 */
	private static class State {
		public boolean bold = false;
		public boolean italic = false;
		public boolean underline = false;
		public boolean strikethrough = false;

		/**
		 * Generates a string that resets the text to the format specified by
		 * this object.
		 * 
		 * @param color
		 *            the color string that specifies "default" format for the
		 *            text
		 * @return the generated string
		 */
		public String generateReset(String color) {
			// Make a string with the reset code + the default string
			StringBuilder sb = new StringBuilder(ChatColor.RESET.toString());
			// Add default formatting
			sb.append(color);
			// Add required properties
			if (bold)
				sb.append(ChatColor.BOLD);
			if (italic)
				sb.append(ChatColor.ITALIC);
			if (underline)
				sb.append(ChatColor.UNDERLINE);
			if (strikethrough)
				sb.append(ChatColor.STRIKETHROUGH);

			return sb.toString();
		}
	}

}
