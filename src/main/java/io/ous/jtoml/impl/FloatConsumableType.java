package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.util.regex.Matcher;

/**
 * A float consumable type of 64bit precision (Double)
 * @author Asafh
 *
 */
final class FloatConsumableType extends RegexConsumableType<Double> {
	FloatConsumableType() {
		super("^([-]?\\d+\\.\\d+)");
	}

	@Override
	protected Double parse(Matcher match) {
		try {
			return Double.parseDouble(match.group());
		}
		catch(IllegalArgumentException iae) {
			throw new ParseException(iae);
		}
	}
}