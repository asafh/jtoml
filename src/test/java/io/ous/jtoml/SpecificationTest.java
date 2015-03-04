package io.ous.jtoml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * This file is under the MIT License, based on https://github.com/agrison/jtoml/blob/master/src/test/java/me/grison/jtoml/SpecificationsTest.java <br/>
 * MIT License: http://opensource.org/licenses/mit-license.php
 * @author Asafh
 *
 */

public class SpecificationTest {
	@Test
	public void testInteger() throws IOException {
		Toml toml = JToml.parseString("foo = 42");
		Assert.assertEquals(42L, toml.getLong("foo").longValue());
	}

	@Test
	public void testDouble() throws IOException {
		Toml toml = JToml.parseString("foo = 3.141592653589793");
		Assert.assertEquals(3.14159265, toml.getDouble("foo").doubleValue(), 0.00001d);
	}

	@Test
	public void testBoolean() throws IOException {
		Toml toml = JToml.parseString("foo = true\nbar = false");
		Assert.assertTrue(toml.getBoolean("foo").booleanValue());
		Assert.assertFalse(toml.getBoolean("bar").booleanValue());
	}

	@Test
	public void testString() throws IOException {
		Toml toml = JToml.parseString("foo = \"Hello\\tWorld\\nI'm having \\\"!\"");
		Assert.assertEquals("Hello\tWorld\nI'm having \"!", toml.getString("foo"));
	}

	@Test
	public void testArray() throws IOException {
		Toml toml = JToml.parseString("foo = [\n\"Hello\",\n\n\t \"World\"\n,\"Nice\"]");
		Assert.assertEquals(Arrays.asList("Hello", "World", "Nice"), toml.getList("foo"));
	}

	@Test(expected = ParseException.class)
	public void testWrongStringEscaping() throws IOException {
		// wrong = "C:\Users\nodejs\templates" # note: doesn't produce a valid path
		// right = "C:\\Users\\nodejs\\templates"
		Toml toml = JToml.parseString("right = \"C:\\\\Users\\\\nodejs\\\\templates\"");
		Assert.assertEquals("C:\\Users\\nodejs\\templates", toml.getValue("right"));
		toml = JToml.parseString("wrong = \"C:\\Users\\nodejs\\templates\"");
	}
	
	@Test(expected = ParseException.class) //Parse exception
	public void testOverwritePreviousKey() throws IOException {
		JToml.parseString("[fruit]\ntype = \"apple\"\n\n[fruit.type]\napple = \"yes\"");
	}

	@Test
	public void testNull() throws IOException {
		Toml toml = JToml.parseString("foo = 1337");
		Assert.assertEquals(null, toml.getValue("bar"));
		Assert.assertEquals(null, toml.getString("bar"));
		Assert.assertEquals(null, toml.getLong("bar"));
		Assert.assertEquals(null, toml.getDouble("bar"));
		Assert.assertEquals(null, toml.getDate("bar"));
		Assert.assertEquals(null, toml.getBoolean("bar"));
		Assert.assertEquals(null, toml.getList("bar"));
	}

	@Test(expected = ClassCastException.class)
	public void testIncompatibleType() throws IOException {
		Toml toml = JToml.parseString("foo = 1337");
		toml.getString("foo");
	}

	@Test
	public void testLocalsAsMap() throws IOException {
		Toml toml = JToml.parseString("[foo]\nbar = true\nbaz = false");
		Map<String, Object> map = toml.getTomlTable("foo").toMap();
		Assert.assertTrue(map.containsKey("bar") && map.get("bar").equals(Boolean.TRUE));
		Assert.assertTrue(map.containsKey("baz") && map.get("baz").equals(Boolean.FALSE));
	}
	@Test
	public void testAsMap() throws IOException {
		Toml toml = JToml.parseString("x=\"A\"\n[foo]\nbar = true\nbaz = false");
		
		Map<String, Object> map = toml.toMap();

        Assert.assertTrue(map.get("foo") instanceof Map);

        Map<String, Object> foo = (Map<String, Object>) map.get("foo");
        Assert.assertEquals(Boolean.TRUE, foo.get("bar"));
        Assert.assertEquals(Boolean.FALSE, foo.get("baz"));
	}

	@Test
	public void testCustomObject() throws IOException {
		Toml toml = JToml.parseString("[foo]\nstringKey=\"a\"\nlongKey=42\ndoubleKey=13.37\n" + //
				"booleanKey=true\nlistKey=[1,2,3]\n[foo.bar]\nbazz=\"Hello\"\ndummy=459\neVal=\"Lorem\"");
		Foo foo = toml.getTomlTable("foo").asObject(Foo.class);
		Assert.assertEquals("a", foo.stringKey);
		Assert.assertEquals(Long.valueOf(42), foo.longKey);
		Assert.assertEquals(Double.valueOf(13.37), foo.doubleKey, 0.00001d);
		Assert.assertEquals(Boolean.TRUE, foo.booleanKey);
		Assert.assertEquals(Arrays.asList(1L, 2L, 3L), foo.listKey);
		Assert.assertEquals("Hello", foo.bar.bazz);
		Assert.assertEquals(Long.valueOf(459), foo.bar.dummy);

        Assert.assertEquals(null, foo.bar.eNull);
        Assert.assertEquals(TestEnum.Lorem, foo.bar.eVal);

		// test no root group
		toml = JToml.parseString("stringKey=\"a\"\nlongKey=42\ndoubleKey=13.37\n" + //
				"booleanKey=true\nlistKey=[1,2,3]\n[bar]\nbazz=\"Hello\"\ndummy=459");
		Assert.assertEquals(foo.toString(), toml.asObject(Foo.class).toString());
	}
    public static enum TestEnum {
        Bla,
        Dog,
        Some,
        Lorem,
        Ipsum
    }
    @Test
    public void testEnum() throws IOException {
        Toml toml = JToml.parseString("[foo]\nstringKey=\"Dog\"\nintVal=3");

        TestEnum value = toml.getAsEnum(TestEnum.class, "foo","stringKey");
        Assert.assertEquals(TestEnum.Dog, value);

        TestEnum intVal = toml.getTomlTable("foo").getAsEnum(TestEnum.class, "intVal");
        Assert.assertEquals(TestEnum.values()[3], intVal);
    }

	/**
	 * Simple class tested above.
	 */
	public static class Bar {
		String bazz;
		Long dummy;
        TestEnum eVal;
        TestEnum eNull;

		@Override
		public String toString() {
			return bazz + dummy;
		}
	}

	public static class Foo {
		String stringKey;
		Long longKey;
		Double doubleKey;
		Boolean booleanKey;
		List<Object> listKey;
		Bar bar;
		Boolean awesome;

		@Override
		public String toString() {
			return stringKey + longKey + doubleKey + booleanKey + listKey + bar + awesome;
		}
	}
}

