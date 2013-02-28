package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.util.regex.Matcher;

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