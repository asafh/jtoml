package io.ous.jtoml;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 * This file is under MIT License, based on https://github.com/agrison/jtoml/blob/master/src/test/java/me/grison/jtoml/ExampleTomlTest.java <br/>
 * MIT License: http://opensource.org/licenses/mit-license.php
 * @author Asafh
 *
 */
public class ExampleTomlTest {
	private static Toml toml;

	@BeforeClass
	public static void parseExampleFile() throws IOException {
        toml = JToml.parse(ExampleTomlTest.class.getResourceAsStream("/example.toml"));
	}

	@Test
	public void testTopLevelString() {
		// title = "TOML Example"
		assertEquals("TOML Example", toml.getString("title"));
	}

	@Test
	public void testNestedStrings() {
		// [owner]
		// name = "Tom Preston-Werner"
		// organization = "GitHub"
		assertEquals("Tom Preston-Werner", toml.getString("owner","name"));
		assertEquals("GitHub", toml.getString("owner","organization"));
	}

	@Test
	public void testNewlineAndHashInString() {
		// [owner]
		// bio = "GitHub Cofounder & CEO\nLikes tater tots and beer #awesome."
		assertEquals(
				"GitHub Cofounder & CEO\nLikes tater tots and beer #awesome.",
				toml.getString("owner","bio"));
	}

	@Test
	public void testDate() throws ParseException, ClassCastException, java.text.ParseException {
		// dob = 1979-05-27T07:32:00Z # First class dates? Why not?
		assertEquals(
				createCalendar("yyyy-MM-dd-HH:mm:ssZ",
						"1979-05-27-07:32:00-0000").getTime(), toml.getDate("owner","dob"));
	}

	@Test
	public void testArray() {
		// [database]
		// server = "192.168.1.1"
		// ports = [ 8001, 8001, 8002 ]
		assertEquals("192.168.1.1", toml.getString("database","server"));
		assertEquals(
				createList(Long.valueOf(8001), Long.valueOf(8001), Long.valueOf(8002)),
                toml.getList("database","ports"));
	}

	@Test
	public void testLong() {
		// [database]
		// connection_max = 5000
		// latency_max = 42 # this is in milliseconds
		assertEquals(Long.valueOf(5000),
				toml.getLong("database","connection_max"));
		assertEquals(Long.valueOf(42), toml.getLong("database","latency_max"));
	}

	@Test
	public void testBoolean() {
		// [database]
		// enabled = true
		// awesome = false # just because
		assertEquals(Boolean.valueOf(true), toml.getBoolean("database","enabled"));
		assertEquals(Boolean.valueOf(false),
				toml.getBoolean("database","awesome"));
	}

	@Test
	public void testIndentationDoesntMatter() {
		// [servers]
		//
		// # You can indent as you please. Tabs or spaces. TOML don't care.
		// [servers.alpha]
		// ip = "10.0.0.1"
		// dc = "eqdc10"
		assertEquals("10.0.0.1", toml.getString("servers","alpha","ip"));
		assertEquals("eqdc10", toml.getString("servers","alpha","dc"));

		// [servers.beta]
		// ip = "10.0.0.2"
		// dc = "eqdc10"
		assertEquals("10.0.0.2", toml.getString("servers","beta","ip"));
		assertEquals("eqdc10", toml.getString("servers","beta","dc"));
	}

	@Test
	public void testArrayOfArrays() {
		// [clients]
		// data = [ ["gamma", "delta"], [1, 2] ] # just an update to make sure
		// parsers support it
		assertEquals(ExampleTomlTest.<Object> createList(
				createList("gamma", "delta"),
				createList(Long.valueOf(1), Long.valueOf(2))),
				toml.getList("clients","data"));
	}

	private static Calendar createCalendar(String pattern, String value)
			throws ParseException, java.text.ParseException {
		Date date = new SimpleDateFormat(pattern).parse(value);
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	private static <T> List<T> createList(T... elements) {
		return Arrays.<T> asList(elements);
	}
}
