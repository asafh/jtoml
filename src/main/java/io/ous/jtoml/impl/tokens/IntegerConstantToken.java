package io.ous.jtoml.impl.tokens;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public class IntegerConstantToken extends AbstractToken implements ValuedToken<Long> {
    private final long value;

    public IntegerConstantToken(long value) {
        super(TokenType.IntegerConstant);
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
