package io.ous.jtoml.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 21:47
 * To change this template use File | Settings | File Templates.
 */
public enum SymbolToken implements Token {
    //Brackets:
    CurlyLeft('{'),		CurlyRight('}'),
    SquareLeft('['),	SquareRight(']'),
    //Delimiters:
    Dot('.'),			Comma(','),
    //Assignments:
    Equals('='),
    //Strings:
    SingleQuote('\''),
    DoubleQuote('\"'),
    Newline('\n');

    private final char symbol;
    private SymbolToken(char symbol) {
        this.symbol = symbol;
    }
    char getSymbol() {
        return symbol;
    }
//    @Override
    public TokenType getType() {
        return TokenType.Symbol;
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }



    private static final Map<Character, SymbolToken> MAPPING;
    static {

        SymbolToken[] values = values();
        MAPPING = new HashMap<Character, SymbolToken>(20);
        for(SymbolToken token : values) {
            MAPPING.put(token.getSymbol(), token);
        }
    }

    /**
     * returns the SymbolToken for the symbol c if exists, otherwise null
     * @param c
     * @return
     */
    public static SymbolToken getSymbolToken(char c) {
        return MAPPING.get(c);
    }

    /**
     * returns the SymbolToken for the symbol c if exists, otherwise throws an IllegalArgumentException
     * @param c
     * @return
     * @throws IllegalArgumentException if there is no symbol token for the character c
     */
    public static SymbolToken valueOf(char c) throws IllegalArgumentException {
        SymbolToken ret = getSymbolToken(c);
        if(ret == null) {
            throw new IllegalArgumentException("No Symbol token for character: "+c);
        }
        return ret;
    }
}
