package com.enjin.officialplugin;

import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/**
 * Handling misc chat/console functions
 */
public class EnjinConsole {
	
	//Chat color codes.
	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");
	
    public static String[] header()
    {
        String[] text = new String[]{
            ChatColor.GREEN + "=== Enjin Minecraft Plugin ==="
        };

        return text;
    }

	public static String translateColorCodes(String string) {
		if (string == null) {
			return "";
		}
	
		String newstring = string;
		newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
		return newstring;
	}
}
