package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;
import io.ous.jtoml.Toml;
import io.ous.jtoml.TomlTable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Parser {
	//Members:
	private final Tokenizer parsedTokens;
	private final TomlTable output;

	public Parser(Reader reader) throws IOException {
		this(reader,new Toml());
	}


	public Parser(Reader reader, TomlTable toml) throws IOException {
        this.parsedTokens = Tokenizer.parse(reader);

		this.output = toml;
//		this.currentTomlTable = toml;
	}

	public TomlTable parse() {
        TomlTable currentTomlTable = output;
        while(parsedTokens.hasNext()) {
            Token token = parsedTokens.peek().token;
            //We can either have an empty line, the beginning of a
            if(token == SymbolToken.Newline) {
                parsedTokens.next();
            }
            else if(token == SymbolToken.SquareLeft) {
                parsedTokens.next();
                if(parsedTokens.nextIfMatch(SymbolToken.SquareLeft) ) {
                    currentTomlTable = onArrayTable();
                }
                else {
                    currentTomlTable = onTable();
                }
            }
            else if(token.getType() == Token.TokenType.Key || token.getType() == Token.TokenType.BasicString) {
                onAssignment(currentTomlTable);
                if(!parsedTokens.nextIfMatch(SymbolToken.Newline)) {
                    throw new ParseException("Newline expected after assignment");
                }
            }
        }
        return output;
	}


    private void onAssignment(TomlTable currentTomlTable) {
        List<String> parts = readKeyParts(SymbolToken.Equals);
        String name = parts.remove(parts.size()-1);
        TomlTable createIn = travelIn(currentTomlTable, parts);//Key names are relative to the current table.

		if(createIn.containsKey(name)) {
			throw new ParseException("Cannot overwrite key "+name);
		}
        Object value = readValue();

		createIn.put(name, value);
	}
    void removeNewlines() {
        while(parsedTokens.hasNext() && parsedTokens.peek().token == SymbolToken.Newline) {
            parsedTokens.next();
        }
    }
    private List<Object> readArray() {
        List<Object> ret = new ArrayList<Object>();
        removeNewlines(); //arrays can have new lines after [
        if(parsedTokens.peek().token == SymbolToken.SquareRight) { //empty
            parsedTokens.next();
            return ret;
        }

        Class<?> type = null;
        while(true) {
            Object value = readValue();
            Class<?> currentType = value.getClass();
            if(type == null) {
                type = currentType;
            }
            else if(!type.equals(currentType)) {
                throw new ParseException("Array types must be equal, "+type.getName()+"!="+currentType.getName());
            }
            ret.add(value);
            removeNewlines(); //arrays can have new lines after values
            if(parsedTokens.nextIfMatch(SymbolToken.Comma) ) {
                removeNewlines(); //arrays can have new lines after commas
                continue;
            }
            if(parsedTokens.nextIfMatch(SymbolToken.SquareRight) ) {
                break;
            }
        }

        return ret;
    }
    private TomlTable readInlineTable() {
        TomlTable ret = new TomlTable();
        if(parsedTokens.nextIfMatch((SymbolToken.CurlyRight)) ) { //empty table.
            return ret;
        }
        while(true) {
            onAssignment(ret);
            if(parsedTokens.nextIfMatch(SymbolToken.Comma) ) {
                continue;
            }
            if(!parsedTokens.nextIfMatch(SymbolToken.CurlyRight)) {
                throw new ParseException("After assignment, ',' or '}' are expected in an inline table.");
            }

            return ret;
        }
    }
    private Object readValue() {
        Token token = parsedTokens.next().token;
        if(token instanceof ValuedToken) {
            return ((ValuedToken) token).getValue();
        }
        if(token == SymbolToken.SquareLeft) {
            return readArray();
        }
        if(token == SymbolToken.CurlyLeft) {
            return readInlineTable();
        }
        throw new ParseException("Unexpected token "+token);
    }

    private List<String> readKeyParts(SymbolToken end) {
        List<String> parts = new ArrayList<String>();
        while(true) {
            Token keyPart = parsedTokens.next().token;
            String key;

            switch(keyPart.getType()) {
                case Key:
                case BasicString:
                    key = ((ValuedToken<String>) keyPart).getValue();
                    break;
                default:
                    throw new ParseException("Key can only have bare keys or basic strings");
            }
            parts.add(key);
            Token dotOrEnd = parsedTokens.next().token;
            if(dotOrEnd == end) {
                break;
            }
            if(dotOrEnd != SymbolToken.Dot) {
                throw new ParseException("Expected '"+end+"' or '.', not "+dotOrEnd);
            }
        }

        return parts;
    }

    private TomlTable diveFromRoot(List<String> names) {
        return travelIn(output, names); //Table names are absolute, not relative
    }
    private TomlTable travelIn(TomlTable start, List<String> names) {
        TomlTable current = start;
        for(String part : names) {
            //Diving in:
            Object value = current.get(part);
            if(value == null) { //New table
                TomlTable child = new TomlTable();
                current.put(part, child);
                current = child;
            }
            else if(value instanceof TomlTable) {
                current = (TomlTable) value;
            }
            else if (value instanceof List) {
                List tableArray = (List) value;
                if(tableArray.isEmpty()) {
                    throw new ParseException("Cannot add to an already defined array"); //only if a = [], [[a]]
                    //otherwise there will already be an item there
                }
                Object arrayItem = tableArray.get(tableArray.size()-1);
                if(arrayItem == null || !(arrayItem instanceof TomlTable)) {
                    throw new ParseException("There is already a non Table Array under "+names); //only if a = [], [[a]]
                }

                current = (TomlTable) arrayItem;
            }
            else {
                throw new ParseException(names+" already has a non-table or array table value named "+part);
            }
        }
        return current;
    }

	private TomlTable onTable() {
        List<String> parts = readKeyParts(SymbolToken.SquareRight);
        return diveFromRoot(parts);
	}
    private TomlTable onArrayTable() {
        List<String> parts = readKeyParts(SymbolToken.SquareRight);
        if(!parsedTokens.nextIfMatch(SymbolToken.SquareRight)) { //checking for second end.
            throw new ParseException("Array table must end with ]]");
        }

        String tableName = parts.remove(parts.size()-1);
        TomlTable currentTomlTable = diveFromRoot(parts); //Table array names are absolute, not relative

        List tableArray = currentTomlTable.getList(tableName);
        if(tableArray == null) {
            tableArray = new ArrayList<TomlTable>();
            currentTomlTable.put(tableName, tableArray);
        }
        else if(!tableArray.isEmpty() && !(tableArray.get(0) instanceof  TomlTable)) {
            throw new ParseException("Cannot add TableArray to an existing Array of other value type.");
        }
        currentTomlTable = new TomlTable();
        tableArray.add(currentTomlTable);
        return currentTomlTable;
    }
}
