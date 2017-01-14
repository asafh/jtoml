package io.ous.jtoml.impl;

import java.util.Date;
import static io.ous.jtoml.impl.Token.TokenType;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * Date: 05/03/15
 * Time: 01:11
 */
public class ValuedToken<T> implements Token {
    private final TokenType type;
    private final T value;

    public TokenType getType() {
        return type;
    }

    ValuedToken(TokenType type, T value) {
        this.type = type;
        this.value = value;
    }

    public T getValue() {
        return value;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ValuedToken<Double> floatToken(double value) {
        return new ValuedToken<Double>(TokenType.FloatConstant, value);
    }
    public static ValuedToken<Date> dateToken(Date value) {
        return new ValuedToken<Date>(TokenType.DateConstant, value);
    }
    public static ValuedToken<Long> integerToken(long value) {
        return new ValuedToken<Long>(TokenType.IntegerConstant, value);
    }
    public static ValuedToken<Boolean> booleanToken(boolean value) {
        return new ValuedToken<Boolean>(TokenType.BooleanConstant, value);
    }

    public static ValuedToken<String> key(String value) {
        return new ValuedToken<String>(TokenType.Key, value);
    }
    public static ValuedToken<String> basicString(String value) {
        return new ValuedToken<String>(TokenType.BasicString, value);
    }
    public static ValuedToken<String> literalString(String value) {
        return new ValuedToken<String>(TokenType.LiteralString, value);
    }
    public static ValuedToken<String> multilineString(String value) {
        return new ValuedToken<String>(TokenType.MultilineString, value);
    }
}
