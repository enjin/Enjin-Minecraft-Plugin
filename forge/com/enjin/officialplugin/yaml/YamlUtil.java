package com.enjin.officialplugin.yaml;

import java.util.List;

/**
 * This class contains utility methods for usage in the YAML handler classes
 * @author Devil Boy
 *
 */
public class YamlUtil {

	/**
	 * Gets the amount of spaces at the beggining of a given String
	 * @param line The String to check
	 * @return The amount of leading spaces
	 */
	public static int getLeadingSpaces(String line) {
		int amount = 0;
		for (char c : line.toCharArray()) {
			if (c == ' ') {
				amount++;
			} else {
				break;
			}
		}
		return amount;
	}
	
	/**
	 * Creates an empty String made up of a specified number of spaces
	 * @param length The length of the String to create
	 * @return A String consisting of space characters
	 */
	public static String emptyString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < length; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}
	
	/**
	 * Converts the given list into a YAML path
	 * @param list The list to convert
	 * @return A YAML path String
	 */
	public static String listToPath(List<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String string : list) {
			if (sb.length() == 0) {
				sb.append(string);
			} else {
				sb.append("." + string);
			}
		}
		return sb.toString();
	}
}
