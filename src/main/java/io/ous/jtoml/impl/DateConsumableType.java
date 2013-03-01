package io.ous.jtoml.impl;

import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Consumes a date in ISO8601 full zulu format 
 * @author Asafh
 *
 */
final class DateConsumableType extends ConsumableType<Date> {
	private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final int ISO8601_FORMAT_LENGTH = ISO8601_FORMAT.replaceAll("'", "").length(); //No quotes
	private static final ThreadLocal<DateFormat> ISO8601DateFormat = new ThreadLocal<DateFormat>() {
		protected DateFormat initialValue() {
			return new SimpleDateFormat(ISO8601_FORMAT);
		};
	};
	@Override
	public ConsumedValue<Date> attemptConsume(String current, BufferedReader reader) {
		if(current.length() < ISO8601_FORMAT_LENGTH) {
			return null;
		}
		String relevant = current.substring(0,ISO8601_FORMAT_LENGTH);
		relevant = relevant.replace("Z","+0000"); //SimpleDateFormat does not allow Z to be included instead of a timezone, see http://stackoverflow.com/a/2202300/777203
		try {
			Date date = ISO8601DateFormat.get().parse(relevant);
			return new ConsumedValue<Date>(date, current, ISO8601_FORMAT_LENGTH);
		}
		catch (java.text.ParseException e) {
			return null; //Not a date
		}
	}
}