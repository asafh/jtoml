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
        Symbol,Identifier;

        /**
         * return the token type's name camel-cased
         * @return
         */
        public String getTokenTypeName() {
            String elementName = name();
            elementName = Character.toLowerCase(elementName.charAt(0))+elementName.substring(1);
            return elementName;
        }
    }

    public TokenType getType();
}
