package io.ous.jtoml;

import java.io.IOException;
import java.util.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
	public void testDates() throws IOException, java.text.ParseException {
		Toml toml = JToml.parseString("dates=[1979-05-27T07:32:00Z,\n" +
                "1979-05-27T00:32:00-07:00, " +
                "1979-05-27T00:32:00.999999-07:00]");
        List found = toml.getList("dates");

        Date last = Utils.createCalendar("1979-05-27-07:32:00-0000");
        last.setTime(last.getTime()+999);
        Assert.assertEquals(Utils.createList(
                Utils.createCalendar("1979-05-27-07:32:00-0000"),
                Utils.createCalendar("1979-05-27-07:32:00-0000"),
                last)

        , found);

    }
    @Test
	public void testBasicString() throws IOException, java.text.ParseException {
		Toml toml = JToml.parseString("str=\"I'm a string. \\\"You can quote me\\\". Name\\tJos\\u00E9\\nLocation\\tSF.\"\n");
        Assert.assertEquals("I'm a string. \"You can quote me\". Name\tJos\u00E9\nLocation\tSF.", toml.get("str"));
    }
    @Test
    public void testLiteralString() throws IOException, java.text.ParseException {
        Toml toml = JToml.parseString("winpath  = 'C:\\Users\\nodejs\\templates'\n" +
                "winpath2 = '\\\\ServerX\\admin$\\system32\\'\n" +
                "quoted   = 'Tom \"Dubs\" Preston-Werner'\n" +
                "regex    = '<\\i\\c*\\s*>'");
        Assert.assertEquals("C:\\Users\\nodejs\\templates", toml.get("winpath"));
        Assert.assertEquals("\\\\ServerX\\admin$\\system32\\", toml.get("winpath2"));
        Assert.assertEquals("Tom \"Dubs\" Preston-Werner", toml.get("quoted"));
        Assert.assertEquals("<\\i\\c*\\s*>", toml.get("regex"));
    }

    @Test
    public void testMultilineString() throws IOException, java.text.ParseException {
        Toml toml = JToml.parseString("key1 = \"The quick brown fox jumps over the lazy dog.\"\n" +
                "\n" +
                "key2 = \"\"\"\n" +
                "The quick brown \\\n" +
                "\n" +
                "\n" +
                "  fox jumps \"over \\\n" +
                "    the \"\"lazy dog.\"\"\"\n" +
                "\n" +
                "key3 = \"\"\"\\\n" +
                "       The quick brown \\\n" +
                "       fox\\t jumps over \\\n" +
                "       the lazy dog.\\\n" +
                "       \"\"\"");

        Assert.assertEquals("The quick brown fox jumps over the lazy dog.", toml.get("key1"));
        Assert.assertEquals("The quick brown fox jumps \"over the \"\"lazy dog.", toml.get("key2"));
        Assert.assertEquals("The quick brown fox\t jumps over the lazy dog.", toml.get("key3"));
    }
    @Test
    public void testLiteralMultilineString() throws IOException, java.text.ParseException {
        Toml toml = JToml.parseString("regex2 = '''I [dw]on't need \\d{2} apples'''\n" +
                "lines  = '''\n" +
                "The first newline is\n" +
                "trimmed in raw strings.\n" +
                "   All other whitespace\n" +
                "   is preserved.\n" +
                "'''");
        Assert.assertEquals("I [dw]on't need \\d{2} apples", toml.get("regex2"));
        Assert.assertEquals("The first newline is\n" +
                "trimmed in raw strings.\n" +
                "   All other whitespace\n" +
                "   is preserved.\n", toml.get("lines"));
    }

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
    @Test
    public void testInlineTable() throws IOException {
        Toml toml = JToml.parseString("[foo]\nname = { first = \"Tom\", last = \"Preston-Werner\" }\n");
        TomlTable foo = toml.getTomlTable("foo", "name");
        Assert.assertEquals("Tom",foo.get("first"));
        Assert.assertEquals("Preston-Werner",foo.get("last"));
    }

    @Test
    public void testFloats() {
        Toml toml = JToml.parseString("# fractional\n" +
                "v=[+1.0\n" +
                ",3.1415\n" +
                ",-0.01\n" +
                "\n" +
                "# exponent\n" +
                ",5e+22\n" +
                ",1e6\n" +
                ",-2E-2\n" +
                "\n" +
                "# both\n" +
                ",6.626e-34]");

        Assert.assertEquals(Utils.createList(1.0, 3.1415, -0.01, 4.9999999999999996E22, 1000000.0, -0.02, 6.626E-34), toml.getList("v"));
    }
    @Test
    public void testIntegers() {
        Toml toml = JToml.parseString("" +
                "v=[1_000,\n" +
                "5_349_221,\n" +
                "1_2_3_4_5,     # valid but inadvisable\n" +
                "+99,\n" +
                "42," +
                "0,\n" +
                "-17]");

        Assert.assertEquals(tomlfy(Utils.createList(1000, 5349221, 12345, 99, 42, 0, -17)), toml.getList("v"));
    }

    @Test
    public void testArrayTable() throws IOException {
        JSONtest("[[products]]\n" +
                "name = \"Hammer\"\n" +
                "sku = 738594937\n" +
                "\n" +
                "[[products]]\n" +
                "\n" +
                "[[products]]\n" +
                "name = \"Nail\"\n" +
                "sku = 284758393\n" +
                "color = \"gray\"",
                "{\n" +
                        "  \"products\": [\n" +
                        "    { \"name\": \"Hammer\", \"sku\": 738594937 },\n" +
                        "    { },\n" +
                        "    { \"name\": \"Nail\", \"sku\": 284758393, \"color\": \"gray\" }\n" +
                        "  ]\n" +
                        "}");

    }
    @Test
    public void testKeyNames() throws IOException {
        JSONtest("[a  . \"table.tater\"]\n" +
                "key = 1\n" +
                "bare_key = 1\n" +
                "bare-key = 1\n" +
                "\n" +
                "\"127.0.0.1\" = 1\n" +
                "\"character encoding\" = 1\n" +
                "\"ʎǝʞ\" = 1",
                "{\"a\":{\n" +
                        "  \"table.tater\": {\n" +
                        "\"key\": 1,\n" +
                        "\"bare_key\": 1,\n" +
                        "\"bare-key\": 1,\n" +
                        "\n" +
                        "\"127.0.0.1\": 1,\n" +
                        "\"character encoding\": 1,\n" +
                        "\"ʎǝʞ\": 1" +
                        "  }\n" +
                        "}}");

    }

    /**
     * Replaces Integers with Longs, Floats with Doubles, recursive.
     * @param o
     * @return
     */
    private Object tomlfy(Object o) {
        if(o != null) {
            if(o instanceof List) {
                List list = (List) o;
                List<Object> ret = new ArrayList<Object>(list.size());
                for(Object value : list) {
                    ret.add(tomlfy(value));
                }
                return ret;
            }
            if(o instanceof Map) {
                Map<String, Object> map = (Map) o;
                Map ret = new HashMap();
                for(Map.Entry entry : map.entrySet()) {
                    ret.put(entry.getKey(), tomlfy(entry.getValue()));
                }
                return ret;
            }
            if(o instanceof Float) {
                return new Double((Float)o);
            }
            if(o instanceof Integer) {
                return new Long((Integer)o);
            }
        }
        return o;
    }


    private void JSONtest(String tomlStr, String json) throws IOException {
        Toml toml = JToml.parseString(tomlStr);
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> expected = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});


        Assert.assertEquals(tomlfy(expected), toml);
    }

    @Test
    public void testNestedArrayTable() throws IOException {
        JSONtest("[[fruit]]\n" +
                "  name = \"apple\"\n" +
                "\n" +
                "  [fruit.physical]\n" +
                "    color = \"red\"\n" +
                "    shape = \"round\"\n" +
                "\n" +
                "  [[fruit.variety]]\n" +
                "    name = \"red delicious\"\n" +
                "\n" +
                "  [[fruit.variety]]\n" +
                "    name = \"granny smith\"\n" +
                "\n" +
                "[[fruit]]\n" +
                "  name = \"banana\"\n" +
                "\n" +
                "  [[fruit.variety]]\n" +
                "    name = \"plantain\"",
                "{\n" +
                        "  \"fruit\": [\n" +
                        "    {\n" +
                        "      \"name\": \"apple\",\n" +
                        "      \"physical\": {\n" +
                        "        \"color\": \"red\",\n" +
                        "        \"shape\": \"round\"\n" +
                        "      },\n" +
                        "      \"variety\": [\n" +
                        "        { \"name\": \"red delicious\" },\n" +
                        "        { \"name\": \"granny smith\" }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"name\": \"banana\",\n" +
                        "      \"variety\": [\n" +
                        "        { \"name\": \"plantain\" }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}");
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

