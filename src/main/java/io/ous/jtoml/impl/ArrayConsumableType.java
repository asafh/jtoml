package io.ous.jtoml.impl;

import static io.ous.jtoml.impl.Utils.trimStartAndComment;
import io.ous.jtoml.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consumes an array of arbitrary types
 * @author Asafh
 *
 */
final class ArrayConsumableType extends ConsumableType<List<Object>> {
	private static final String ARRAY_ENDED_UNEXPECTEDLY = "Array ended unexpectedly";
	private static final char ARRAY_START = '[';
	private static final char ARRAY_END = ']';
	private static final char ARRAY_ITEM_DELIMITER = ',';
	@Override
	public ConsumedValue<List<Object>> attemptConsume(String remainingInLine,
			BufferedReader reader) throws IOException {
		if(!Utils.startsWithCharacter(remainingInLine, ARRAY_START)) {
			return null;
		}
		remainingInLine = trimStartAndComment(remainingInLine.substring(1));
		List<Object> ret = new ArrayList<Object>();
		ConsumedValue<?> item = null;
		Class<?> type = null;
		
		while(! Utils.startsWithCharacter(remainingInLine, ARRAY_END)) { //While we haven't reached the array end
			remainingInLine = readIfNeeded(remainingInLine,reader); //Check if we have to get a new line because this one is done
			if(Utils.isEmpty(remainingInLine)) {
				throw new ParseException(ARRAY_ENDED_UNEXPECTEDLY);
			}
			
			item = ConsumableType.readValue(remainingInLine, reader); //Consume one item
			ret.add(item.getValue());
			
			//Verify same type
			Class<?> newType = item.getValue().getClass();
			if(type == null) { //asign type once
				type = newType;
			}
			else if(!type.equals(newType)) {
				throw new ParseException("Array value type changed from "+type+" to "+newType);
			}
			
			
			//Continue in line or get next line
			remainingInLine = item.getRemaining(); //skip over consumed characters
			remainingInLine = readIfNeeded(remainingInLine,reader);
			if(Utils.isEmpty(remainingInLine)) {
				throw new ParseException(ARRAY_ENDED_UNEXPECTEDLY);
			}
			
			//If end of array
			if(Utils.startsWithCharacter(remainingInLine, ARRAY_END)) {
				break;
			}
			else if(!Utils.startsWithCharacter(remainingInLine, ARRAY_ITEM_DELIMITER)) { //otherwise must have an array delimiter
				throw new ParseException("No comma between array items: "+remainingInLine);
			}
			else {
				remainingInLine = trimStartAndComment(remainingInLine.substring(1)); //skip over the comma
				//next while condition checks again if this wasn't a trailing comma and we actually did arrive at the end. 
			}
		}
		remainingInLine = remainingInLine.substring(1); //removing array end
		
		return new ConsumedValue<List<Object>>(ret, remainingInLine);
	}

	private String readIfNeeded(String remainingInLine,
			BufferedReader reader) throws IOException {
		while(remainingInLine.length() == 0) {
			remainingInLine = reader.readLine();
			if(remainingInLine == null) { //EOF
				return "";
			}
			remainingInLine = Utils.trimStartAndComment(remainingInLine);
		}
		return remainingInLine;
	}
}