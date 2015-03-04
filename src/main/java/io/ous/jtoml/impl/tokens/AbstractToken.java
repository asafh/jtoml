package io.ous.jtoml.impl.tokens;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 21:27
 * To change this template use File | Settings | File Templates.
 */
public class AbstractToken implements Token {
    private final TokenType type;
    public AbstractToken(TokenType type) {
        this.type = type;
    }

//    @Override
    public TokenType getType() {
        return type;
    }
}
