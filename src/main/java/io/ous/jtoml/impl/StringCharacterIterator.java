package io.ous.jtoml.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:02
 *
 */
class StringCharacterIterator {
    private final String value;
    protected int at;
    public StringCharacterIterator(String value) {
        this.value = value;
        at = 0;
    }

    public String peekIfMatches(Pattern pattern) {
        String rest = peekAll();
        Matcher matcher = pattern.matcher(rest);
        if(matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    public String nextIfMatches(Pattern pattern) {
        String ret = peekIfMatches(pattern);
        if(ret != null) {
            advance(ret.length());
        }
        return ret;
    }


    public int length() {
        return value.length();
    }

    public int length(String seq) {
        return seq.length();
    }

    public Character peek() {
        return value.charAt(at);
    }

    public String peek(int count) {
        return value.substring(at, at+count);
    }

    public String peekAll() {
        return value.substring(at);
    }

    protected boolean seqEquals(String a, String b) {
        return a == null ? a == b : a.equals(b);
    }
    public boolean hasNext() {
        return hasNext(1);
    }
    public boolean hasNext(int count) {
        return at + count - 1 < length();
    }

    public char next() {
        char value = peek();
        at++;
        return value;
    }
    public String next(int count) {
        String ret = peek(count);
        at += count;
        return ret;
    }
    public boolean peekIfSeqEquals(String check) {
        int length = length(check);
        return hasNext(length) && seqEquals(peek(length),check);
    }
    public boolean peekIfEquals(char check) {
        if(!hasNext()) {
            return false;
        }
        char peek = peek();
        return peek == check;
    }

    public boolean nextIfEquals(char check) {
        if(peekIfEquals(check)) {
            next(1);
            return true;
        }
        return false;
    }

    public boolean nextIfSeqEquals(String check) {
        if(peekIfSeqEquals(check)) {
            next(length(check));
            return true;
        }
        return false;
    }

    public void advance(int length) {
        at += length;
    }

    public int currentIndex() {
        return at;
    }
}
