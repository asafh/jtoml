package io.ous.jtoml.impl.tokens;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public class DateToken extends AbstractToken implements ValuedToken<Date> {
    private final Date value;

    public DateToken(Date value) {
        super(TokenType.DateConstant);
        this.value = value;
    }

    public Date getValue() {
        return value;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
