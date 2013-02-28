package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.io.BufferedReader;

/**
 * Consumes an escaped string
 * @author Asafh
 *
 */
final class StringConsumableType extends ConsumableType<String> {
	private static final char STRING_START = '"';
	@Override
	public ConsumedValue<String> attemptConsume(String line,
			BufferedReader reader) {
		StringBuilder bld = new StringBuilder();
		int index = 0;
		char current = line.charAt(index);
		++index;
		if(current != STRING_START) {
			return null;
		}
		
		
		boolean inEscape = false; //whether this iteration's value is to be understood as an escaped character (e.g. n for \n)
		int lineLength = line.length();
		while(true) {
			if(index >= lineLength) {
				throw new ParseException("String terminated unexpectedly");
			}
			current = line.charAt(index);
			++index;
			if(inEscape) { //e.g. for the 'n' part of a '\n' we need to return the proper value ('\n') and reset the escape state
				current = unescape(current);
				inEscape = false;
			}
			else {
				if(current == '\\') { //Starting escape, do not append this character
					inEscape = true;
					continue;
				}
				if(current == '\"') { //End of string
					break;
				}
			}
			bld.append(current);
		}
		int consumed = index; //since index is always 1 past the last index we consumed, and we start at 0, we consumed 'index' characters
		return new ConsumedValue<String>(bld.toString(), line, consumed);  
	}

	private char unescape(char current) {
		switch(current) {
		case 't': return '\t';
		case 'n': return '\n';
		case 'r': return '\r';
		case '\\': return '\\';
		case '\"': return '\"';
		case '0': return '\0';
		default:
			throw new ParseException("Illegal escape character \\\""+current+"\"");
		}
	}
}