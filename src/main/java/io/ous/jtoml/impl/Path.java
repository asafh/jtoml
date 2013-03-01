package io.ous.jtoml.impl;

import java.util.regex.Pattern;

/**
 * Utility class for handling keys paths
 * @author Asafh
 */
public class Path {
	private Path() {}
	public static final char KEYGROUP_DELIMITER = '.';
	private static final Pattern KEYGROUP_DELIMITER_PATTERN = Pattern.compile("[\\.]+");
	public static String join(String... items) {
		StringBuilder bld = new StringBuilder();
		for(String item : items) {
			item = Utils.trim(item);
			if("".equals(item)) {
				continue;
			}
			bld.append(item).append('.');
		}
		if(bld.length() == 0) {
			return "";
		}
		return bld.substring(0,bld.length()-1); //Removing trailing .
	}
	/**
	 * Returns an array of the components in the key
	 * @param qualifiedKey
	 * @return
	 */
	public static String[] split(String qualifiedKey) {
		qualifiedKey = Utils.trim(qualifiedKey);
		return KEYGROUP_DELIMITER_PATTERN.split(qualifiedKey);
	}
	/**
	 * Returns a String[] of size two, arr[1] containing the last component in this key,
	 * and arr[0] containing the path up that component (or the empty String if none such)
	 * @param qualifiedKey
	 * @return
	 */
	public static String[] parts(String qualifiedKey) {
		qualifiedKey = Utils.trim(qualifiedKey);
		int index = qualifiedKey.lastIndexOf(KEYGROUP_DELIMITER);
		if(index == -1) {
			return new String[] {"",qualifiedKey}; //Root Value
		}
		String group = qualifiedKey.substring(0,index),
				name = qualifiedKey.substring(index+1);
		return new String[] {group,name};
	}
}
