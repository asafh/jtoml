package io.ous.jtoml.impl;

class Utils {
	private static final char COMMENT_START = '#';
	private Utils() {}
	/**
	 * Removes whitespaces from both ends of the string
	 * @param keygroup
	 * @return
	 */
	public static String trim(String string) {
		return string.trim(); //TODO: Is this right? Might not adhere to spec right
	}
	
	/**
	 * Method to check if a string contains a character, slightly faster than checking if it contains the String "c"
	 * @param str
	 * @param ch
	 * @return
	 */
	public static boolean containsCharacter(String str, char ch) {
		return str.indexOf(ch)!=-1;
	}
	/**
	 * Checks if the string str starts with the given character
	 * @param current
	 * @param stringStart
	 * @return
	 */
	public static boolean startsWithCharacter(String str, char stringStart) {
		return str.length() > 0 && str.charAt(0) == stringStart;
	}
	/**
	 * Removes whitespace characters from the beginning of the String. <br/>
	 * If a comment is reached while removing whitespaces, an empty string is returned since the line is considered empty
	 * @param line
	 * @return
	 */
	public static String trimStartAndComment(String line) {
		if(line == null) {
			return null;
		}
		int check = 0;
		while(check < line.length() && Character.isWhitespace(line.charAt(check))) {
			check++;
		}
		if(check < line.length() && line.charAt(check) == COMMENT_START) {
			return "";
		}
		return line.substring(check);
	}
	/**
	 * Returns true iff str is null or the empty string
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

}
