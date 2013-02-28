package io.ous.jtoml.impl;

import java.util.HashMap;
import java.util.Map;

class KeyGroup {
	
	/**
	 * 
	 */
	private final String name;
	private final KeyGroup parent;
	private final Map<String, Object> localValues;
	private final String path;
	public KeyGroup(KeyGroup parent, String name) {
		this.name = name;
		this.parent = parent;
		this.path = parent == null ? name : Path.join(parent.getPath(),name);
		localValues = new HashMap<String, Object>();
	}
	public String getName() {
		return name;
	}
	public KeyGroup getParent() {
		return parent;
	}
	public boolean hasKey(String name) {
		return localValues.containsKey(name);
	}
	public void set(String name, Object value) {
		if(name.indexOf('.') != -1) {
			throw new IllegalArgumentException("Local value cannot contain a dot");
		}
		localValues.put(name, value);
	}
	/**
	 * Returns a local value in this KeyGroup
	 * @param name
	 * @return
	 */
	public Object get(String name) {
		return "".equals(name) ? this : localValues.get(name);
	}
	
	/*
	 * Gets the keygroup with the given (qualified) key
	 * @param qualifiedGroup the qualified keygroup key. e.g. "x.y.z"
	 * @param createGroups create groups leading to that value (but not the value itself)
	 * @return
	 
	public Object getRelativeValue(String qualifiedName,boolean createGroups) {
		KeyGroup targetGroup = this;
		String directories = Path.getGroupName(qualifiedKey);
		for(String nextDir : Path.split(directories)) {
			if(targetGroup.hasKey(nextDir)) {
				try {
					targetGroup = (KeyGroup) targetGroup.get(nextDir);
				}
				catch(ClassCastException cce) {
					throw new IllegalArgumentException("Tried to access a value as a group.");
				}
			}
			else {
				if(createGroups) {
					targetGroup = new KeyGroup(targetGroup, nextDir);
				}
				else {
					return null; //No value
				}
			}
		}
		int innerGroupEndIndex = qualifiedName.indexOf(KEYGROUP_DELIMITER);
		if(innerGroupEndIndex == -1) { //local value
			return get(qualifiedName);
		}
		Object inner =
		if(hasKeyGroup(qualifiedGroup)) {
			return map.get(qualifiedGroup);
		}
		KeyGroup ret;
		
		if(parentEndIndex == -1) { //root keygroup
			ret = new KeyGroup(this, qualifiedGroup);
		}
		else {
			String parentKey = qualifiedGroup.substring(0,parentEndIndex); //without trailing dot
			String groupName = qualifiedGroup.substring(parentEndIndex+1); //without starting dot
			KeyGroup parent = assertKeyGroup(parentKey);
			ret = new KeyGroup(this, parent,groupName);
		}
		map.put(qualifiedGroup, ret);
		return ret;
	}*/
	
	public KeyGroup assertKeygroup(String name) {
		try {
			KeyGroup ret = (KeyGroup) get(name);
			if(ret == null) {
				ret = new KeyGroup(this, name);
				set(name, ret);
			}
			return ret;
		}
		catch(ClassCastException cce) {
			throw new IllegalArgumentException(Path.join(getPath(),name) + " already has a non-keygroup value");
		}
	}
	public String getPath() {
		return path;
	}
	@Override
	public String toString() {
		//return "{\""+getPath()+"\": \""+this.localValues+"\"}";
		return localValues.toString();
	}
	
	public Map<String,Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		toMap(map);
		return map;
	}
	private void toMap(Map<String, Object> map) {
		for(Map.Entry<String, Object> entry : localValues.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();

			if(value != null && value instanceof KeyGroup) {
				((KeyGroup)value).toMap(map);
				//value = ((KeyGroup)value).toMap();
			}
			else {
				map.put(Path.join(getPath(),name), value);				
			}
		}
	}
}