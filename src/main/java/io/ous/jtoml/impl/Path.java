package io.ous.jtoml.impl;

public class Path {
	public static final char KEYGROUP_DELIMITER = '.';
	private static final String KEYGROUP_DELIMITER_REGEX = "[\\.]";
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
	public static String[] split(String qualifiedKey) {
		qualifiedKey = Utils.trim(qualifiedKey);
		return qualifiedKey.split(KEYGROUP_DELIMITER_REGEX);
	}
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
	public static String getGroupName(String qualifiedKey) {
		return parts(qualifiedKey)[0];
	}
	public static String getValueName(String qualifiedKey) {
		return parts(qualifiedKey)[1];
	}
}
