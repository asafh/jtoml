package io.ous.jtoml.impl.tokens;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public class FloatConstantToken extends AbstractToken implements ValuedToken<Double> {
    private final double value;

    public FloatConstantToken(Double value) {
        super(TokenType.FloatConstant);
        this.value = value;
    }

    public Double getValue() {
        return value;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
