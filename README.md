jtoml
=====

[TOML](https://github.com/mojombo/toml) Parser for Java

Using jtoml
----

## Parsing
The JToml class has methods for parsing Toml from files, input streams, readers are just strings  
jtoml does not load the entire toml file before parsing, only the current line being read is kept in memory

```java
Toml toml = JToml.parseString("foo = 42"); //Parsing a String
toml = JToml.parse(ExampleTomlTest.class.getResourceAsStream("/example.toml")); //From an input stream
toml = JToml.parse(new File("/example.toml")); //From a file
toml = JToml.parse(new StringReader("foo = 42")); // From a reader
```

## Getting values
Parsing a TOML file returns a Toml instance, which is the root toml table.
TomlTables have methods to return local values (directly in this TomlTable)
or a relative value (e.g. getting "y.z." from TomlTable "x" will return the value for "x.y.z").

```java
toml.getValue("foo"); // Object
toml.getString("foo"); // String
toml.getBoolean("foo"); // Boolean
toml.getDate("foo"); // Date
toml.getDouble("foo"); // Double
toml.getLong("foo"); // Long
toml.getList("foo"); // List<Object>
toml.getTomlTable("foo"); // TomlTable

toml.getLocalValue("foo"); // returns the value for "foo", dots are not allowed
toml.getLocalXXX("foo"); //....

```

## Mapping to POJO
You can create a POJO class instance, filling it with values for a given TomlTable.
Any field that isn't a type of String, Date, List, Boolean, Double, Long is assumed to be a nested POJO type and is
recursively mapped (it's value in the toml should be a TomlTable).
```toml
[foo]
stringKey="a"
longKey=42
doubleKey=13.37
booleanKey=true
listKey=[1,2,3]
[foo.bar]
bazz="Hello"
dummy=459
```
Retrieving 
```java
class Foo {
	String stringKey;
	Long longKey;
	Double doubleKey;
	Boolean booleanKey;
	List<Object> listKey;
	Bar bar;
	Boolean awesome;
}
class Bar {
  String bazz;
  Long dummy;
}
Foo foo = toml.getTomlTable("foo").asObject(Foo.class);
```

License
-----
[Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)
