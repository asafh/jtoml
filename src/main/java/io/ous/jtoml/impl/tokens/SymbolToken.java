package io.ous.jtoml.impl.tokens;

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
    //Operators:

    @OperatorSymbol		Plus('+'),
    @OperatorSymbol		Equals('='),
    @OperatorSymbol		Minus('-'),

    SingleQuote('\''),
    DoubleQuote('\"'),
    Underscore('_'),
    Backslash('\\'),
    Newline('\n');

    private final char symbol;
    private SymbolToken(char symbol) {
        this.symbol = symbol;
    }
    public char getSymbol() {
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

    public boolean isOperator() {
        try {
            return SymbolToken.class.getField(name()).isAnnotationPresent(OperatorSymbol.class);
        } catch (SecurityException e) {
            throw new IllegalStateException(e); //will not occur, we are checking for a member of our own class
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e); //will not occur, we are checking for looking for ourselves, must exist.
        }
    }

    /**
     * Marker annotation, is assigned to SymbolTokens which are also operators (may also have other meanins, e.q. = is both an operator and assignment)
     * @author Asafh
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    private static @interface OperatorSymbol {

    }


    private static final Map<Character, SymbolToken> MAPPING;
    static {
        MAPPING = new HashMap<Character, SymbolToken>(30);
        for(SymbolToken token : values()) {
            MAPPING.put(token.getSymbol(), token);
        }
    }
    private static Map<Character, SymbolToken> getMapping() {
        return MAPPING;
    }

    /**
     * returns the SymbolToken for the symbol c if exists, otherwise null
     * @param c
     * @return
     */
    public static SymbolToken getSymbolToken(char c) {
        return getMapping().get(c);
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
