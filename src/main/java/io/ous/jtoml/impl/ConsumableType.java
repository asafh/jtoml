package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 
 * A class that represents a type consumable value (e.g. String, Date, Array), a consumable type reads consumes an unknown
 * number of characters from a String and returns it's value
 * @author Asafh
 *
 * @param <T>
 * @throws IOException if an IO error occurred while trying to read more lines
 * @throws ParseException if the value is deemed to be of this type but could not be parsed properly
 */
public abstract class ConsumableType<T> {
	public abstract ConsumedValue<T> attemptConsume(String current, BufferedReader reader) throws IOException, ParseException;
	
	
	/**
	 * Reads the value from current and reader if this consumable type can be read from the next available characters. <br/>
	 * If the current string doesn't hold a consumable value for this type, null will be returned.
	 * In case the data spreads over more than one line reader can be called to get more lines, <b>Reader should not be advanced if the value cannot be consumed</b>
	 * @param current
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static ConsumedValue<?> readValue(String current, BufferedReader reader) throws IOException {
		for(ConsumableType<?> type : TYPES) {
			ConsumedValue<?> ret = type.attemptConsume(current, reader);
			if(ret != null) {
				return ret;
			}
		}
		throw new ParseException("Could not identify value "+current);
	}
	/**
	 * The set of ConsumableTypes available by the order in which consuming will be attempted
	 */
	private static ConsumableType<?>[] TYPES = new ConsumableType<?>[] {
		new ConstantConsumableType<Boolean>(Boolean.TRUE,"true"), //Constants are the fastest
		new ConstantConsumableType<Boolean>(Boolean.FALSE,"false"), //Constants are the fastest
		new StringConsumableType(), //String identifies by first character
		new ArrayConsumableType(), //Array identifies by first character
		new DateConsumableType(), //Date must be before Integer otherwise Integer will consume the year number.
		new FloatConsumableType(), //Float must be before Integer otherwise Integer will consume the first part
		new IntegerConsumableType()
	};
}
