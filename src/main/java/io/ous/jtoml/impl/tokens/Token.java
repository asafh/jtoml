package io.ous.jtoml.impl.tokens;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public interface Token {
    public static enum TokenType {
        Key,
        MultilineString, BasicString, LiteralString,
        BooleanConstant, FloatConstant, IntegerConstant, DateConstant,
        Symbol;
    }

    public TokenType getType();
}
