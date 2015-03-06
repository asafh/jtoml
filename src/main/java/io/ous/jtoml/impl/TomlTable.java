package io.ous.jtoml.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TomlTable extends HashMap<String, Object> {
	public TomlTable() {

	}

	/**
	 * Returns the object in the given path or null if it does not exist
	 * @param key the key under this table
     * @param keys subsequent keys, each is either a string for a property name in a Table or an integer for an index in an Array
	 * @return
	 * @throws IllegalArgumentException if one of the keys goes through a value (such as a String), or isn't an index for going through an array
	 */
	public Object get(String key, Object... keys) throws IllegalArgumentException {
		Object value = get(key);
        if(keys != null) {
            for(Object key2 : keys) {
                if(value instanceof TomlTable) {
                    value = ((TomlTable)value).get(key2);
                }
                else if (value instanceof List && key2 instanceof Integer) {
                    value = ((List) value).get((Integer) key2);
                }
                else {
                    throw new IllegalArgumentException(key2+" is not a valid property under a "+value.getClass().getName());
                }
            }
        }
        return value;
	}

    /**
     * Convenience method, mainly to keep the old signature
     * @param bar
     * @return
     */
    public Object getValue(String bar, Object... keys) {
        return get(bar, keys);
    }

	public String getString(String name, Object... keys) throws ClassCastException {
		return (String) get(name, keys);
	}
	public Long getLong(String name, Object... keys) throws ClassCastException {
		return (Long) get(name, keys);
	}
	public Double getDouble(String name, Object... keys) throws ClassCastException {
		return (Double) get(name, keys);
	}
	public Date getDate(String name, Object... keys) throws ClassCastException {
		return (Date) get(name, keys);
	}
	public List<?> getList(String name, Object... keys) throws ClassCastException {
		return (List<?>) get(name, keys);
	}
    public List<TomlTable> getArrayTable(String name, Object... keys) throws ClassCastException {
		return (List<TomlTable>) get(name, keys);
	}
	public Boolean getBoolean(String name, Object... keys) throws ClassCastException {
		return (Boolean) get(name, keys);
	}
	public TomlTable getTomlTable(String name, Object... keys) throws ClassCastException {
		return (TomlTable) get(name, keys);
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
    public <T extends Enum<T>> T getAsEnum(Class<T> type, String name, Object... keys) {
        Object value = get(name, keys);
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
	 * Returns an unmodifiable map of this Toml Table. <br/>
	 * The map has an entry for every property directly on this table.
	 * @return
	 */
	public Map<String,Object> toMap() {
        return Collections.unmodifiableMap(this);
	}
}