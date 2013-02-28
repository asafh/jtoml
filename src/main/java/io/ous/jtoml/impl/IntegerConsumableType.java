package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.util.regex.Matcher;

/**
 * An integer consumable type of 64bit precision (Long)
 * @author Asafh
 *
 */
final class IntegerConsumableType extends RegexConsumableType<Long> {
	IntegerConsumableType() {
		super("^([-]?\\d+)");
	}

	@Override
	protected Long parse(Matcher match) {
		try {
			return Long.parseLong(match.group());
		}
		catch(IllegalArgumentException iae) {
			throw new ParseException(iae);
		}
	}
}