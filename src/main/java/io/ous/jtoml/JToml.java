package io.ous.jtoml;

import io.ous.jtoml.impl.Parser;

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
    
    public static Toml parse(File file) throws FileNotFoundException, IOException {
    	return parse(new FileInputStream(file));
	}
    public static Toml parse(InputStream input) throws FileNotFoundException, IOException {
    	return parse(new InputStreamReader(input, Charset.forName("UTF-8"))); //Force UTF-8
	}
    
    public static Toml parseString(String config) {
        try {
            return parse(new StringReader(config));
        } catch (IOException ex) {
            throw new IllegalStateException("StringReader should never throw an IOException",ex);
        }
    }
    
    public static Toml parse(Reader reader) throws IOException {
    	return new Parser(reader).parse();   	
    }
}
