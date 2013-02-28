package io.ous.jtoml;

import io.ous.jtoml.impl.Parser;
import io.ous.jtoml.Toml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

public class JToml {
    
    public static Toml parseToml(File file) throws FileNotFoundException, IOException {
    	return parseToml(new FileInputStream(file));
	}
    public static Toml parseToml(InputStream input) throws FileNotFoundException, IOException {
    	return parseToml(new InputStreamReader(input, Charset.forName("UTF-8"))); //Force UTF-8
	}
    
    public static Toml parseToml(String config) throws IOException {
    	return parseToml(new StringReader(config));
	}
    
    public static Toml parseToml(Reader reader) throws IOException {
    	return new Parser(reader).parse();   	
    }
}
