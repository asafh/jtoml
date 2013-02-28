package io.ous.jtoml.impl;

import java.io.BufferedReader;

/**
 * Consumes a constant String from the input and returns a constant value
 * @author Asafh
 *
 * @param <T>
 */
class ConstantConsumableType<T> extends ConsumableType<T> {
	private final T value;
	private final String constant;

	public ConstantConsumableType(T value, String constant) {
		this.value = value;
		this.constant = constant;
	}

	@Override
	public ConsumedValue<T> attemptConsume(String current, BufferedReader reader) {
		return current.startsWith(constant) ? new ConsumedValue<T>(value, current, constant.length()) : null;
	}
}