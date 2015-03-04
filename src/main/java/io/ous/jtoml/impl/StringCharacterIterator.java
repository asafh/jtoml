package io.ous.jtoml.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class StringCharacterIterator extends AdvancedIterator<Character, String> {
    private final String value;
    public StringCharacterIterator(String value) {
        this.value = value;
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


    @Override
    public int length() {
        return value.length();
    }

    @Override
    public int length(String seq) {
        return seq.length();
    }

    @Override
    public Character peek() {
        return value.charAt(at);
    }

    @Override
    public String peek(int count) {
        return value.substring(at, at+count);
    }

    @Override
    public String peekAll() {
        return value.substring(at);
    }
}
