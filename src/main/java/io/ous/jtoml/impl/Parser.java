package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;
import io.ous.jtoml.Toml;
import io.ous.jtoml.impl.SymbolToken;
import io.ous.jtoml.impl.Token;
import io.ous.jtoml.impl.ValuedToken;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Parser {
	//Members:
	private final TokensAdvancedIterator parsedTokens;
	private final TomlTable output;
	private TomlTable currentTomlTable;
	

	public Parser(Reader reader) throws IOException {
		this(reader,new Toml());
	}
    private static class TokensAdvancedIterator extends AdvancedIterator<Tokenizer.ParsedToken, List<Tokenizer.ParsedToken>> {

        private final List<Tokenizer.ParsedToken> parsedTokenList;

        public TokensAdvancedIterator(List<Tokenizer.ParsedToken> parsedTokenList) {
            if(parsedTokenList == null) {
                throw new NullPointerException();
            }
            this.parsedTokenList = parsedTokenList;
        }

        @Override
        public int length() {
            return parsedTokenList.size();
        }

        @Override
        public int length(List<Tokenizer.ParsedToken> seq) {
            return seq.size();
        }

        @Override
        public Tokenizer.ParsedToken peek() {
            return parsedTokenList.get(at);
        }

        @Override
        public List<Tokenizer.ParsedToken> peek(int count) {
            return parsedTokenList.subList(at,at+count);
        }
        protected boolean matches(SymbolToken symbol, Tokenizer.ParsedToken ptoken) {
            return ptoken.token == symbol;
        }

        public boolean peekIfMatch(SymbolToken symbol) {
            return matches(symbol, peek());
        }
        public boolean nextIfMatch(SymbolToken symbol) {
            if(matches(symbol, peek())) {
                next();
                return true;
            }
            return false;
        }
    }

	public Parser(Reader reader, TomlTable toml) throws IOException {
        final List<Tokenizer.ParsedToken> parsedTokenList = Tokenizer.parse(reader);
        this.parsedTokens = new TokensAdvancedIterator(parsedTokenList);

		this.output = toml;
		this.currentTomlTable = toml;
	}

	public TomlTable parse() {
        while(parsedTokens.hasNext()) {
            Token token = parsedTokens.peek().token;
            //We can either have an empty line, the beginning of a
            if(token == SymbolToken.Newline) {
                parsedTokens.next();
            }
            else if(token == SymbolToken.SquareLeft) {
                parsedTokens.next();
                if(parsedTokens.nextIfMatch(SymbolToken.SquareLeft) ) {
                    onArrayTable();
                }
                else {
                    onTable();
                }
            }
            else if(token.getType() == Token.TokenType.Key || token.getType() == Token.TokenType.BasicString) {
                onAssignment();
                if(!parsedTokens.nextIfMatch(SymbolToken.Newline)) {
                    throw new ParseException("Newline expected after assignment");
                }
            }
        }
        return output;
	}


    private void onAssignment() {
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
            onAssignment();
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

    private void diveFromRoot(List<String> names) {
        currentTomlTable = travelIn(output, names); //Table names are absolute, not relative
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

	private void onTable() {
        List<String> parts = readKeyParts(SymbolToken.SquareRight);
        diveFromRoot(parts);
	}
    private void onArrayTable() {
        List<String> parts = readKeyParts(SymbolToken.SquareRight);
        if(!parsedTokens.nextIfMatch(SymbolToken.SquareRight)) { //checking for second end.
            throw new ParseException("Array table must end with ]]");
        }

        String tableName = parts.remove(parts.size()-1);
        diveFromRoot(parts); //Table array names are absolute, not relative

        List<TomlTable> tableArray = (List<TomlTable>) currentTomlTable.getList(tableName);
        if(tableArray == null) {
            tableArray = new ArrayList<TomlTable>();
        }
        else if(!tableArray.isEmpty() && !(tableArray.get(0) instanceof  TomlTable)) {
            throw new ParseException("Cannot add TableArray to an existing Array of other value type.");
        }
        currentTomlTable = new TomlTable();
        tableArray.add(currentTomlTable);
    }
}
