package io.ous.jtoml.impl;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds common logic for ConsumableTypes that identify values by regexes and acts on the matching group.
 * @author Asafh
 *
 * @param <T>
 */
abstract class RegexConsumableType<T> extends ConsumableType<T> {
	private final Pattern pattern;
	public RegexConsumableType(String regex) {
		pattern = Pattern.compile(regex);
	}
	@Override
	public ConsumedValue<T> attemptConsume(String current, BufferedReader reader) {
		Matcher match = pattern.matcher(current);
		if(match.find()) {
			int end = match.end();
			return new ConsumedValue<T>(parse(match), current, end) ;
		}
		return null;
	}
	protected abstract T parse(Matcher match);
}