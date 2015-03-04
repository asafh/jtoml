package io.ous.jtoml.impl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectDeserializer {
	private static final Set<Class<?>> PRIMITIVES;
	static {
		Set<Class<?>> primitives = new HashSet<Class<?>>();
		for(Class<?> type : new Class<?>[] {String.class, Date.class, List.class, Boolean.class, Double.class, Long.class}) {
			primitives.add(type);
		}
		PRIMITIVES = Collections.unmodifiableSet(primitives);
	}
	private static final ObjectDeserializer INSTANCE = new ObjectDeserializer(); //No need to lazy load.
	public static ObjectDeserializer getInstance() {
		return INSTANCE;
	}
	/**
	 * Creates an object of type <code>type</code> initializing fields using the given TomlTable
	 * @param type
	 * @param group
	 * @return
	 */
	public<T> T create(Class<T> type, TomlTable group) {
		T ret = instantiate(type);
		visitFields(ret, group);
		return ret;
	}
	/**
	 * Creates an object of type <code>type</code> using the value of the key named <code>keyName</code> under the given TomlTable
	 * @param type The type of object to create
	 * @param containingTable The table containing the value representing the created Object
	 * @param keyName The name in the containingTable from which to read the value.
	 * @return
	 */
    @SuppressWarnings("unchecked")
	<T> T createField(Class<T> type, TomlTable containingTable, String keyName) {
		if(PRIMITIVES.contains(type)) {
			return type.cast(containingTable.get(keyName));
		}
        else if(type.isEnum()) {
            return (T) containingTable.getAsEnum(type.asSubclass(Enum.class), keyName);
        }
		else {
			TomlTable target = containingTable.getTomlTable(keyName);
			return target == null ? null : create(type,target);
		}
	}
	void visitFields(Object obj, TomlTable groupForObject) {
		Class<?> clz = obj.getClass();
		while(!Object.class.equals(clz)) {
			visitClassFields(obj, groupForObject, clz);
			clz = clz.getSuperclass();
		}
	}
	void visitClassFields(Object obj, TomlTable groupForObject, Class<?> clz) {
		for(Field field : clz.getDeclaredFields()) {
			Object value = createField(field.getType(), groupForObject, field.getName());
			if(value != null) { //If has value
				field.setAccessible(true);
				try {
					field.set(obj, value);
				} catch (Exception e) {
					throw new IllegalStateException("Could not assign value to field "+field +" at "+clz,e);
				}
			}
		}
	}
	<T> T instantiate(Class<T> type) {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Could not instantiate "+type,e);
		}
	}
    /**
     * Returns an instance of the enum T corresponding to the value given
     * If the value is null, null will be returned
     * If the value is a long, the enum constant with that ordinal is returned
     * If the value is a String, the enum constant is returned using the enum's valueOf method.
     * @param type the class of the enum type
     * @param value the value to convert to the enum
     * @param <T> the enum type
     * @return
     */
    public <T extends Enum<T>> T toEnum(Class<T> type, Object value) {
        if(value == null) {
            return null;
        }
        if(value instanceof Long) {
            int ordinal = ((Long) value).intValue();
            return type.getEnumConstants()[ordinal];
        }
        if(value instanceof String) {
            return Enum.valueOf(type,(String)value);
        }

        throw new IllegalArgumentException("Value is neither String or Long but "+value.getClass().getCanonicalName());
    }
}
