package io.ous.jtoml.impl;

/**
 *
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 21:28
 */
public interface Token {
    public static enum TokenType {
        Key,
        MultilineString, BasicString, LiteralString,
        BooleanConstant, FloatConstant, IntegerConstant, DateConstant,
        Symbol
    }

    public TokenType getType();
}

