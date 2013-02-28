package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.util.regex.Matcher;

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