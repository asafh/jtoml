package io.ous.jtoml.impl.tokens;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public class BooleanConstantToken extends AbstractToken implements ValuedToken<Boolean> {
    private final boolean value;

    public BooleanConstantToken(boolean value) {
        super(TokenType.BooleanConstant);
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
