package io.ous.jtoml.impl;

import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AdvancedListIterator<T, TSequence> implements ListIterator<T> {
    protected int at;

    public abstract int length();
    public abstract int length(TSequence seq);
    public abstract T peek();
    public abstract TSequence peek(int count);
    public TSequence peekAll() {
        return peek(length()-at);
    }

    protected boolean equals(T a, T b) {
        return a == null ? a == b : a.equals(b);
    }
    protected boolean seqEquals(TSequence a, TSequence b) {
        return a == null ? a == b : a.equals(b);
    }

    public boolean hasNext() {
        return hasNext(1);
    }
    public boolean hasNext(int count) {
        return at + count - 1 < length();
    }

    public T next() {
        T value = peek();
        at++;
        return value;
    }
    public TSequence next(int count) {
        TSequence ret = peek(count);
        at += count;
        return ret;
    }
    public boolean peekIfSeqEquals(TSequence check) {
        int length = length(check);
        if(hasNext(length) && seqEquals(peek(length),check)) {
            return true;
        }
        return false;
    }
    public boolean peekIfEquals(T check) {
        if(!hasNext()) {
            return false;
        }
        T peek = peek();
        return equals(peek, check);
    }

    public boolean nextIfEquals(T check) {
        if(peekIfEquals(check)) {
            next(1);
            return true;
        }
        return false;
    }

    public boolean nextIfSeqEquals(TSequence check) {
        if(peekIfSeqEquals(check)) {
            next(length(check));
            return true;
        }
        return false;
    }

    public void advance(int length) {
        at += length;
    }

    public boolean hasPrevious() {
        return at > 0;
    }

    public T previous() {
        at--;
        return peek();
    }

    public int nextIndex() {
        return at+1;
    }

    public int previousIndex() {
        return at-1;
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove through StringCharacterListIterator");
    }

    public void set(T character) {
        throw new UnsupportedOperationException("Cannot set through StringCharacterListIterator");
    }

    public void add(T character) {
        throw new UnsupportedOperationException("Cannot add through StringCharacterListIterator");
    }
}
