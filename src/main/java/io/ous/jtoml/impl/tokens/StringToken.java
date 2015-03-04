package io.ous.jtoml.impl.tokens;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class StringToken extends AbstractToken  implements ValuedToken<String> {
    private final String value;

    private StringToken(TokenType type, String value) {
        super(type);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return value;
    }

    public static StringToken key(String value) {
        return new StringToken(TokenType.Key, value);
    }
    public static StringToken basic(String value) {
        return new StringToken(TokenType.BasicString, value);
    }
    public static StringToken literal(String value) {
        return new StringToken(TokenType.LiteralString, value);
    }
    public static StringToken multiline(String value) {
        return new StringToken(TokenType.MultilineString, value);
    }
}
