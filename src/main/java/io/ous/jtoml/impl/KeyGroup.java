package io.ous.jtoml.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyGroup {
	
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
	public Map<String, Object> localsAsMap() {
		return Collections.unmodifiableMap(localValues);
	}
	/**
	 * Returns the path to this keygroup (i.e. it's qualified key)
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	public boolean hasLocalKey(String name) {
		return localValues.containsKey(name);
	}
	public void setLocalValue(String name, Object value) {
		validateLocalKey(name);
		localValues.put(name, value);
	}
	/**
	 * Returns a local value in this KeyGroup
	 * @param name
	 * @return
	 */
	public Object getLocalValue(String name) {
		validateLocalKey(name);
		return "".equals(name) ? this : localValues.get(name);
	}
	/**
	 * Returns the object in the given path or null if it does not exist
	 * @param qualifiedKey
	 * @return
	 * @throws IllegalArgumentException if the key goes through a non-keygroup item (Such as a String)
	 */
	public Object getValue(String qualifiedKey) throws IllegalArgumentException {
		KeyGroup in = this;
		String[] parts = Path.parts(qualifiedKey);
		for(String child : Path.split(parts[0])) {
			try {
				in = in.getLocalKeyGroup(child);
				if(in == null) {
					return null;
				}
			}
			catch(ClassCastException cce) {
				throw new IllegalArgumentException(qualifiedKey+" goes through a non-keygroup value at "+Path.join(in.getPath(),child));
			}
		}
		return in.getLocalValue(parts[1]);
	}
	
	/**
	 * Throws an IllegalArgumentException if <code>name</code> is not a local name (i.e. contains a dot)
	 * @param name
	 */
	private void validateLocalKey(String name) {
		if(Utils.containsCharacter(name, Path.KEYGROUP_DELIMITER)) {
			throw new IllegalArgumentException("Local key cannot contain a dot");
		}
	}
	
	/**
	 * Returns a child keygroup with the given (local) name, creating it if it does not exist yet
	 * @param name the name to get or create the keygroup with
	 * @return
	 * @throws IllegalArgumentException if a local value with the given name already exists but is not a keygroup
	 */
	public KeyGroup assertKeygroup(String name) {
		try {
			KeyGroup ret = getLocalKeyGroup(name);
			if(ret == null) {
				ret = new KeyGroup(this, name);
				setLocalValue(name, ret);
			}
			return ret;
		}
		catch(ClassCastException cce) {
			throw new IllegalArgumentException(Path.join(getPath(),name) + " already has a non-keygroup value");
		}
	}
	
	@Override
	public String toString() {
		return "["+getPath()+"]";
	}
	
	/**
	 * Perform a traversal on this keygroup and all subkeys
	 * @param visitor
	 */
	public void traverse(TomlVisitor visitor) {
		visitor.visitPreorderKeygroup(this);
		for(Map.Entry<String, Object> entry : localValues.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			if(value instanceof KeyGroup) {
				((KeyGroup) value).traverse(visitor);
			}
			else {
				visitor.visitLeaf(this, name, value);
			}
		}
		visitor.visitPostorderKeygroup(this);
	}
	
	public String getString(String name) throws ClassCastException {
		return (String) getValue(name);
	}
	public Long getLong(String name) throws ClassCastException {
		return (Long) getValue(name);
	}
	public Double getDouble(String name) throws ClassCastException {
		return (Double) getValue(name);
	}
	public Date getDate(String name) throws ClassCastException {
		return (Date) getValue(name);
	}
	public List<?> getList(String name) throws ClassCastException {
		return (List<?>) getValue(name);
	}
	public Boolean getBoolean(String name) throws ClassCastException {
		return (Boolean) getValue(name);
	}
	public KeyGroup getKeyGroup(String name) throws ClassCastException {
		return (KeyGroup) getValue(name);
	}

    /**
     *
     * @param name
     * @param type
     * @param <T>
     * @return
     * @see KeyGroup#getLocalAsEnum(String, Class)
     */
    public <T extends Enum<T>> T getAsEnum(String name, Class<T> type) {
        Object value = getValue(name);
        return ObjectDeserializer.getInstance().toEnum(type,value);
    }
	
	public String getLocalString(String name) throws ClassCastException {
		return (String) getLocalValue(name);
	}
	public Long getLocalLong(String name) throws ClassCastException {
		return (Long) getLocalValue(name);
	}
	public Double getLocalDouble(String name) throws ClassCastException {
		return (Double) getLocalValue(name);
	}
	public Date getLocalDate(String name) throws ClassCastException {
		return (Date) getLocalValue(name);
	}
	public List<?> getLocalList(String name) throws ClassCastException {
		return (List<?>) getLocalValue(name);
	}
	public Boolean getLocalBoolean(String name) throws ClassCastException {
		return (Boolean) getLocalValue(name);
	}
	public KeyGroup getLocalKeyGroup(String name) throws ClassCastException {
		return (KeyGroup) getLocalValue(name);
	}

    /**
     * Returns an instance of the enum T corresponding to the value in the local key <i>name</i>
     * If the local value is null, null will be returned
     * If the local value is a long, the enum constant with that ordinal is returned
     * If the local value is a String, the enum constant is returned using the enum's valueOf method.
     * @param name
     * @param type
     * @param <T>
     * @return
     * @throws IllegalArgumentException if the value is neither of type String or Integer
     * @see ObjectDeserializer#toEnum(Class, Object)
     */
    public <T extends Enum<T>> T getLocalAsEnum(String name, Class<T> type) {
        Object value = getLocalValue(name);
        return ObjectDeserializer.getInstance().toEnum(type,value);
    }
	
	/**
	 * Returns an instance of <code>type</code> filled by the properties here
	 * @param type
	 * @return
	 */
	public<T> T asObject(Class<T> type) {
		return ObjectDeserializer.getInstance().create(type, this);
	}
	
	
	/**
	 * Returns an exploded map of this Toml.
	 * this is very memory intensive: it creates a map for every key group, containing all of the values
	 * of it's keygroups under it flat ("b.c" :val) and a map for any keygroup under it ("b": { c: val}). <br/>
	 * If you need this sort of accessibilty, juse use Toml's getX methods rather than exploding the values into a map. <br/>
	 * <i>This was only done for playing around, and isn't really needed</i> 
	 * @return
	 */
	public Map<String,Object> toExplodedMap() {
		final Map<String, Object> map = new HashMap<String, Object>();
		this.traverse(new TomlVisitor() {
			@SuppressWarnings("unchecked")
			private void setValue(KeyGroup in, String localName, Object value) {
				String fullpath = Path.join(in.getPath(),localName);
				int delimIndex = -1;
				while(true) {
					delimIndex = fullpath.indexOf(Path.KEYGROUP_DELIMITER, delimIndex+1);
					if(delimIndex == -1) {
						map.put(fullpath,value);
						break;
					}
					else {
						String groupKey = fullpath.substring(0,delimIndex);
						String valueKey = fullpath.substring(delimIndex+1);
						((Map<String, Object>)map.get(groupKey)).put(valueKey, value);
					}
				}
			}
			public void visitLeaf(KeyGroup in, String localName, Object value) {
				setValue(in, localName, value);
			}

			public void visitPreorderKeygroup(KeyGroup group) {
				if(group != KeyGroup.this) {
					map.put(group.getPath(), new HashMap<String, Object>());
				}
			}

			public void visitPostorderKeygroup(KeyGroup group) {
				
			}
		});
		return map;
	}
	
	/**
	 * Returns a map of this Toml. <br/>
	 * The map has an entry for every assignment (full key -> value) and for every keygroup (implicit keygroups included) (keygroup key -> KeyGroup) 
	 * @return
	 */
	public Map<String,Object> toMap() {
		final Map<String, Object> map = new HashMap<String, Object>();
		this.traverse(new TomlVisitor() {
			private void setValue(KeyGroup in, String relativePath, Object value) {
				String fullpath = Path.join(in.getPath(),relativePath);
				map.put(fullpath, value);
			}
			public void visitLeaf(KeyGroup in, String localName, Object value) {
				setValue(in, localName, value);
			}

			public void visitPreorderKeygroup(KeyGroup group) {
				if(group != KeyGroup.this) {
					setValue(KeyGroup.this, group.getPath(), group);
				}
			}

			public void visitPostorderKeygroup(KeyGroup group) {
				
			}
		});
		return map;
	}
}