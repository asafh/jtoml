package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;
import io.ous.jtoml.impl.tokens.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * DateConstant: 01/03/15
 * Time: 21:22
 * To change this template use File | Settings | File Templates.
 */
class Tokenizer {
    private static final String BREAK_LINE = System.getProperty("line.separator");
    private static final String LITERAL_MULTILINE_STRING_DELIMITER = "\'\'\'";
    private static final String MULTILINE_STRING_DELIMITER = "\"\"\"";
    private static final char BASIC_STRING_DELIMITER = '\"';
    private static final char COMMENT_START = '#';
    private final BufferedReader reader;

    public static List<ParsedToken> parse(Reader reader) throws IOException {
        Tokenizer tokenizer = new Tokenizer(reader);
        return tokenizer.parsedTokens;
    }

    private static BufferedReader buffer(Reader reader) {
        return (BufferedReader) (reader instanceof BufferedReader ? reader : new BufferedReader(reader));
    }
    static class ParsedToken {
        final Token token;
        final int line;
        final int chars;

        ParsedToken(Token token, int line, int chars) {
            this.token = token;
            this.line = line;
            this.chars = chars;
        }

        @Override
        public String toString() {
            return token.getType() + " at " + this.line+":"+this.chars+" "+token;
        }

    }

    /**
     * 1979-05-27T07:32:00Z
     1979-05-27T00:32:00-07:00
     1979-05-27T00:32:00.999999-07:00
     */
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|(-\\d{2}:\\d{2})|\\.\\d{6}-\\d{2}:\\d{2})");
    /**
     * # fractional
     +1.0
     3.1415
     -0.01

     # exponent
     5e+22
     1e6
     -2E-2

     # both
     6.626e-34
     */
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^\\d+(_\\d+)*((\\.\\d+(_\\d+)*)|(?=E))(E(\\+|-)?\\d+(_\\d+)*)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+");

    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    //Result
    private final List<ParsedToken> parsedTokens;

    //Tokenizing State:
    private StringCharacterIterator chars;
    private boolean eof;
    private int currentLine;


    public Tokenizer(Reader reader) throws IOException {
        this.reader = buffer(reader);
        parsedTokens = new ArrayList<ParsedToken>();

        currentLine = 0;
        eof = false;
        nextRawLine();

        while(chars.hasNext() || !eof) {
            //No need to use whitespaces between tokens.
            int lineIndex = currentLine;
            int charIndex = chars.currentIndex();
            Token token = parseNext();
            if(token != null) {
                parsedTokens.add(new ParsedToken(token, lineIndex,charIndex));
            }
        }
    }
    private void nextRawLine() throws IOException {
        currentLine++;
        String line = reader.readLine();
        if(line == null) {
            eof = true;
            line = "";
        }
        chars = new StringCharacterIterator(line);
    }

    private char nextRawChar() throws IOException {
        if(!chars.hasNext()) {
            nextRawLine();
            return '\n';
        }
        return chars.next();
    }
    private Token parseMultilineString() throws IOException {
        StringBuilder builder = new StringBuilder();
        char c = nextRawChar();

        while(true) {
            switch(c) {
                default: //Normal character
                    if(c == '\n') {
                        builder.append(BREAK_LINE); //Normalizing to OS.
                    }
                    else {
                        builder.append(c);
                    }
                    break;

                case '\\':
                    c = nextRawChar();
                    if(c == '\n') {
                        //Escaping the new line, skip until next non-whitespace
                        while(Character.isWhitespace(c)) {
                            c = nextRawChar();
                        }
                        //Now we have a character we don't need to ignore, re-enter the logic:
                        continue;
                    }
                    else {
                        String unescaped = unescapeCharacter(c);
                        builder.append(unescaped);
                    }
                    break;
                case '\"':
                    char c2 = nextRawChar();
                    if(c2 == '\"') {
                        char c3 = nextRawChar();
                        if(c3 == '\"') { //END:
                            return StringToken.multiline(builder.toString());
                        }
                        else { //Whoops, not really the end, only ""
                            builder.append(c); //appending c1
                            builder.append(c2); //appending c2
                            c = c3; //dealing with c3
                        }
                    }
                    else {
                        builder.append(c); //appending c1
                        c = c2; //dealing with c2
                    }
                    break;
            }
        }
    }
    private Token parseBasicString() {
        StringBuilder builder = new StringBuilder();
        while(chars.hasNext()) {
            char c = chars.next();
            switch(c) {
                case '\\':
                    char escaped = chars.next(); //NoSuchElement...
                    String unescaped = unescapeCharacter(escaped);
                    builder.append(unescaped);
                    break;
                default: //Normal character
                    builder.append(c);
                    break;
                case '\"': //END:
                    return StringToken.basic(builder.toString());

            }
        }
        throw new ParseException("No ending quote for basic string.");
    }

    private String unescapeCharacter(char escaped) {
        String unicode;
        switch(escaped) {
            case 'b':
                return "\b";
            case 't':
                return "\t";
            case 'n':
                return "\n";
            case 'f':
                return "\f";
            case 'r':
                return "\r";
            case '\"':
                return "\"";
            case '\\':
                return "\\";
            case 'u':
                unicode = chars.next(4);
                break;
            case 'U':
                unicode = chars.next(4);
                break;
            default:
                throw new ParseException("Reserved escaped character '"+escaped+"'");

        }
        try {
            int code = Integer.parseInt(unicode, 16);
            return new String(Character.toChars(code));
        }
        catch(NumberFormatException nfe) {
            throw new ParseException("Bad escape code "+unicode);
        }

    }

    private Token parseLiteralString() {
        StringBuilder builder = new StringBuilder();
        while(chars.hasNext()) {
            char c = chars.next();
            if(c == '\'') { //END:
                return StringToken.literal(builder.toString());
            }
            else {
                builder.append(c);
            }
        }
        throw new ParseException("No ending single quote for literal string.");
    }
    private Token parseMultilineLiteralString() throws IOException {
        StringBuilder builder = new StringBuilder();
        while(true) {
            if(chars.nextIfSeqEquals("\'\'\'")) { //always in the same line
                return StringToken.literal(builder.toString());
            }
            else {
                builder.append(nextRawChar());
            }
        }
    }

    /**
     * 1979-05-27T07:32:00Z
     * 1979-05-27T00:32:00-07:00
     * 1979-05-27T00:32:00.999999-07:00
     * @param group
     * @return
     */
    private Token parseDateToken(String group) {
        String pattern;
        switch(group.length()) {
            case 20:
                group = group.replace("Z","+0000"); //SimpleDateFormat does not allow Z to be included instead of a timezone, see http://stackoverflow.com/a/2202300/777203
                pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
                break;
            case 25:
                pattern = "yyyy-MM-dd'T'HH:mm:ssX";
                break;
            case 32:
                pattern = "yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSX";
                break;
            default: throw new ParseException("Bad date format:"+group);
        }

        SimpleDateFormat parser = new SimpleDateFormat(pattern);
        try {
            Date date = parser.parse(group);
            return new DateToken(date);
        } catch (java.text.ParseException e) {
            throw new ParseException("Couldn't parse date "+group, e);
        }
    }
    private Token parseFloatToken(String group) {
        return new FloatConstantToken(Double.valueOf(group));
    }
    private Token parseIntegerToken(String group) {
        return new IntegerConstantToken(Long.valueOf(group));
    }

    private Token parseNext() throws IOException {
        if(!chars.hasNext() || chars.nextIfEquals(COMMENT_START)) {
            nextRawLine();
            return SymbolToken.Newline; //Always adding a new line for the last line.
        }
        if(Character.isWhitespace(chars.peek())) {
            chars.next();
            return null;
        }


        if(chars.nextIfSeqEquals(MULTILINE_STRING_DELIMITER)) {
            if(!chars.hasNext()) { //Ignore newline character immediately after """
                nextRawLine();
            }
            return parseMultilineString();
        }

        if(chars.nextIfEquals(BASIC_STRING_DELIMITER)) {
            return parseBasicString();
        }
        if(chars.nextIfSeqEquals(LITERAL_MULTILINE_STRING_DELIMITER)) {
            if(!chars.hasNext()) { //Ignore newline character immediately after '''
                nextRawLine();
            }
            return parseMultilineLiteralString();
        }

        if(chars.nextIfEquals('\'')) {
            return parseLiteralString();
        }

        char peek = chars.peek();
        SymbolToken symbol = SymbolToken.getSymbolToken(peek);
        if(symbol != null) {
            chars.next();
            return symbol;
        }

        if(chars.nextIfSeqEquals("true")) {
            return new BooleanConstantToken(true);
        }
        if(chars.nextIfSeqEquals("false")) {
            return new BooleanConstantToken(false);
        }

        String match;
        if(Character.isDigit(peek)) { //tiny tweak, begin with numbers:.
            if((match = chars.nextIfMatches(DATE_PATTERN)) != null) {
                return parseDateToken(match);
            }
            if((match = chars.nextIfMatches(FLOAT_PATTERN)) != null) {
                return parseFloatToken(match);
            }
            //This has the assumption keys never start with a digit.
            if((match = chars.nextIfMatches(INTEGER_PATTERN)) != null) {
                return parseIntegerToken(match);
            }
        }
        if((match = chars.nextIfMatches(KEY_PATTERN)) != null) {
            return StringToken.key(match);
        }

        throw new ParseException("Unexpected input: "+chars.peekAll());
    }
}
