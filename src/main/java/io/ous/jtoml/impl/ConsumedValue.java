package io.ous.jtoml.impl;

/**
 * A ConsumedValue holds a consumed value and the remainder of the input after consuming it (or the empty string if none)
 * @author Asafh
 *
 * @param <T>
 */
final class ConsumedValue<T> {
	/**
	 * A ConsumedValue with the given value and with the
	 * remainder of characters remaining at <code>current</code> after <code>consumed</code> characters have been read
	 * @param value
	 * @param current
	 * @param consumed
	 */
	public ConsumedValue(T value, String current, int consumed) {
		this(value,current.substring(consumed));
	}
	/**
	 * A ConsumedValue with the given value and with the given remaining input
	 * @param value
	 * @param remaining
	 */
	public ConsumedValue(T value, String remaining) {
		this.value = value;
		this.remaining = remaining;
	}
	private final T value;
	private final String remaining;
	
	/**
	 * Returns the consumed value
	 * @return
	 */
	public T getValue() {
		return value;
	}
	/**
	 * Returns the remaining input (with {@link Utils#trimStartAndComment(String)} called on it. <br/>
	 * Never returns null, no input remaining will return the empty string.
	 * @return
	 */
	public String getRemaining() {
		return Utils.trimStartAndComment(remaining);
	}
}