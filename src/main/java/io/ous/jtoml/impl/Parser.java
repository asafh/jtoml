package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;
import io.ous.jtoml.Toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class Parser {
	private static final char ASSIGNMENT_DELIMITOR = '=';
	
	private static final char KEYGROUP_START = '[';
	private static final char KEYGROUP_END = ']';
	private static final int NOT_FOUND = -1;
	
	//Members:
	private final BufferedReader reader;
	private final Toml output;
	private KeyGroup currentKeygroup;
	

	public Parser(Reader reader) {
		this(reader,new Toml());
	}

	public Parser(Reader reader, Toml toml) {
		this.reader = buffer(reader);
		this.output = toml;
		this.currentKeygroup = toml;
	}

	private static BufferedReader buffer(Reader reader) {
		return (BufferedReader) (reader instanceof BufferedReader ? reader : new BufferedReader(reader));
	}
	private String nextLine() throws IOException {
		return reader.readLine();
	}
	public Toml parse() throws IOException {
		String line = reader.readLine();
		
		while(line != null) {
			onLine(line);
			line = nextLine();
		}
		return output;
		
	}
	

	private void onLine(String line) throws IOException {
		line = Utils.trimStartAndComment(line);
		if(line.length() == 0) {
			return;
		}
		switch(line.charAt(0)) {
		case KEYGROUP_START:
			onKeygroup(line);
			return;
		default: //Key assignment
			onAssignment(line);
			return;
		}
	}
	private KeyGroup buildPath(KeyGroup start, String path) {
		return buildPath(start, Path.split(path));
	}
	private KeyGroup buildPath(KeyGroup start, String[] path) {
		if(path.length == 0) {
			return start;
		}
		KeyGroup createIn = start;
		for(String item : path) {
			createIn = createIn.assertKeygroup(item);
		}
		return createIn;
	}
	private void onAssignment(String line) throws IOException {
		int assignIndex = line.indexOf(ASSIGNMENT_DELIMITOR);
		if(assignIndex == NOT_FOUND) {
			throw new ParseException("Assignment line must have the equals '=' character");
		}
		String qualifiedName = line.substring(0,assignIndex);
		String[] parts = Path.parts(qualifiedName);
		
		
		KeyGroup createIn = buildPath(currentKeygroup, parts[0]); //Values are relative to the current keygroup
		
		String name = parts[1];
		if(createIn.hasLocalKey(name)) {
			throw new ParseException("Cannot overwrite key "+name);
		}
		
		String strValue = line.substring(assignIndex+1);
		strValue = Utils.trimStartAndComment(strValue);
		ConsumedValue<?> value = ConsumableType.readValue(strValue, reader);
		String remaining = value.getRemaining();
		if(Utils.trimStartAndComment(remaining).length() > 0) {
			throw new ParseException("Extra characters at "+remaining);
		}
		
		createIn.setLocalValue(name, value.getValue());
	}
	
	private void onKeygroup(String line) {
		if(line.charAt(0) != KEYGROUP_START || line.charAt(line.length()-1) != KEYGROUP_END) {
			throw new ParseException("Keygroup line does not end with ]");
		}
		String keygroup = line.substring(1, line.length()-1);
		keygroup = Utils.trim(keygroup); //removing spaces
		
		String[] parts = Path.parts(keygroup);
		
		KeyGroup createIn = buildPath(output, parts[0]); //Keygroups are always relative to the root
		
		String name = parts[1];
		if(createIn.hasLocalKey(name)) {
			throw new ParseException("Cannot overwrite key "+name);
		}
		currentKeygroup = createIn.assertKeygroup(name);
	}
	public void close() throws IOException {
		reader.close();
	}
}
