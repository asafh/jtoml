package io.ous.jtoml.impl.tokens;

/**
 * User: Asafh
 * Date: 04/03/15
 * Time: 01:04
 * To change this template use File | Settings | File Templates.
 */
public interface ValuedToken<T> extends Token {
    public T getValue();
}
