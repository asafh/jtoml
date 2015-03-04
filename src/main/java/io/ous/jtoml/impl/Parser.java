package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;
import io.ous.jtoml.Toml;
import io.ous.jtoml.impl.tokens.StringToken;
import io.ous.jtoml.impl.tokens.SymbolToken;
import io.ous.jtoml.impl.tokens.Token;
import io.ous.jtoml.impl.tokens.ValuedToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Parser {
	private static final char ASSIGNMENT_DELIMITOR = '=';
	
	private static final char KEYGROUP_START = '[';
	private static final char KEYGROUP_END = ']';
	private static final int NOT_FOUND = -1;
	
	//Members:
	private final TokenListListIterator parsedTokens;
	private final Toml output;
	private TomlTable currentKeygroup;
	

	public Parser(Reader reader) throws IOException {
		this(reader,new Toml());
	}
    private static class TokenListListIterator extends AdvancedListIterator<Tokenizer.ParsedToken, List<Tokenizer.ParsedToken>> {

        private final List<Tokenizer.ParsedToken> parsedTokenList;

        public TokenListListIterator(List<Tokenizer.ParsedToken> parsedTokenList) {
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
        protected boolean matches(Object pattern, Tokenizer.ParsedToken ptoken) {
            if(pattern == null) {
                return true;
            }
            if(pattern instanceof Token.TokenType) {
                return ptoken.token.getType() == pattern;
            }
            if(pattern instanceof SymbolToken) {
                return ptoken.token == pattern;
            }
            return false;
        }

        public Tokenizer.ParsedToken peekIfMatches(Object pattern) {
            Tokenizer.ParsedToken peek = peek();
            return matches(pattern, peek) ? peek : null;
        }
        public Tokenizer.ParsedToken nextIfMatches(Object pattern) {
            Tokenizer.ParsedToken peek = peek();
            return matches(pattern, peek) ? next() : null;
        }
        public List<Tokenizer.ParsedToken> peekIfMatches(Object... pattern) {
            List<Tokenizer.ParsedToken> rest = peek(pattern.length);
            for(int i = 0; i < pattern.length; ++i) {
                if(!matches(pattern[i], rest.get(i))) {
                    return null;
                }
            }

            return rest;
        }
        public List<Tokenizer.ParsedToken> nextIfMatches(Object... pattern) {
            List<Tokenizer.ParsedToken> ret = peekIfMatches(pattern);
            if(ret != null) {
                advance(ret.size());
            }
            return ret;
        }
    }

	public Parser(Reader reader, Toml toml) throws IOException {
        final List<Tokenizer.ParsedToken> parsedTokenList = Tokenizer.parse(reader);
        this.parsedTokens = new TokenListListIterator(parsedTokenList);

		this.output = toml;
		this.currentKeygroup = toml;
	}

	private static BufferedReader buffer(Reader reader) {
		return (BufferedReader) (reader instanceof BufferedReader ? reader : new BufferedReader(reader));
	}

	public Toml parse() throws IOException {
        while(parsedTokens.hasNext()) {
            Token token = parsedTokens.peek().token;
            //We can either have an empty line, the beginning of a
            if(token == SymbolToken.Newline) {
                parsedTokens.next();
                continue;
            }
            if(token == SymbolToken.SquareLeft) {
                parsedTokens.next();
                if(parsedTokens.nextIfMatches(SymbolToken.SquareLeft) != null) {
                    onArrayTable();
                }
                else {
                    onTable();
                }
                continue;
            }
            if(token.getType() == Token.TokenType.Key || token.getType() == Token.TokenType.BasicString) {
                onAssignment();
                if(parsedTokens.nextIfMatches(SymbolToken.Newline) == null) {
                    throw new ParseException("Newline expected after assignment");
                }
                continue;
            }
        }

		return output;
		
	}


    private void onAssignment() {
        List<String> parts = readKeyParts(SymbolToken.Equals);
        String name = parts.remove(parts.size()-1);
        TomlTable createIn = travelIn(currentKeygroup, parts);//Key names are relative to the current table.

		if(createIn.containsKey(name)) {
			throw new ParseException("Cannot overwrite key "+name);
		}
        Object value = readValue(name);

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
            Object value = readValue(String.valueOf(ret.size()));
            Class<?> currentType = value.getClass();
            if(type == null) {
                type = currentType;
            }
            else if(!type.equals(currentType)) {
                throw new ParseException("Array types must be equal, "+type.getName()+"!="+currentType.getName());
            }
            ret.add(value);
            removeNewlines(); //arrays can have new lines after values
            if(parsedTokens.nextIfMatches(SymbolToken.Comma) != null) {
                removeNewlines(); //arrays can have new lines after commas
                continue;
            }
            if(parsedTokens.nextIfMatches(SymbolToken.SquareRight) != null) {
                break;
            }
        }

        return ret;
    }
    private TomlTable readInlineTable(String name) {
        TomlTable ret = new TomlTable();
        if(parsedTokens.nextIfMatches((SymbolToken.CurlyRight)) != null) { //empty table.
            return ret;
        }
        while(true) {
            onAssignment();
            if(parsedTokens.nextIfMatches(SymbolToken.Comma) != null) {
                continue;
            }
            if(parsedTokens.nextIfMatches(SymbolToken.CurlyRight) == null) {
                throw new ParseException("After assignment, ',' or '}' are expected in an inline table.");
            }

            return ret;
        }
    }
    private Object readValue(String name) {
        Token token = parsedTokens.next().token;
        if(token instanceof ValuedToken) {
            return ((ValuedToken) token).getValue();
        }
        if(token == SymbolToken.SquareLeft) {
            return readArray();
        }
        if(token == SymbolToken.CurlyLeft) {
            return readInlineTable(name);
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
                    key = ((StringToken) keyPart).getValue();
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
        currentKeygroup = travelIn(output, names); //Table names are absolute, not relative
    }
    private TomlTable travelIn(TomlTable start, List<String> names) {
        TomlTable current = start;
        for(String part : names) {
            //Diving in:
            Object value = current.get(part);
            if(value == null) { //New keygroup
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
                throw new ParseException(names+" already has a non-keygroup or array table value named "+part);
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
        if(parsedTokens.nextIfMatches(SymbolToken.SquareRight) == null) { //checking for second end.
            throw new ParseException("Array table must end with ]]");
        }

        String tableName = parts.remove(parts.size()-1);
        diveFromRoot(parts); //Table array names are absolute, not relative

        List<TomlTable> tableArray = (List<TomlTable>) currentKeygroup.getList(tableName);
        if(tableArray == null) {
            tableArray = new ArrayList<TomlTable>();
        }
        currentKeygroup = new TomlTable();
        tableArray.add(currentKeygroup);
    }
}
