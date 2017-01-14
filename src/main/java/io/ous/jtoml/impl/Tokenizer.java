package io.ous.jtoml.impl;

import io.ous.jtoml.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
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
    private static final char LITERAL_STRING_DELIMITER = '\'';
    private static final String MULTILINE_STRING_DELIMITER = "\"\"\"";
    private static final char BASIC_STRING_DELIMITER = '\"';
    private static final char COMMENT_START = '#';
    private final BufferedReader reader;

    public static Tokenizer parse(Reader reader) throws IOException {
        return new Tokenizer(reader);
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
    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(\\.(\\d{1,6}))?(Z|-\\d{2}:\\d{2})");
    private static final int DATE_PATTERN_GROUP_DATETIME = 1;
    private static final int DATE_PATTERN_GROUP_FRACTION = 3;
    private static final int DATE_PATTERN_GROUP_TZ = 4;
    private static final int DATE_FRACTION_PERCISION = 6;
    private static final int DATE_FRACTION_MAX = 1000000;
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
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
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^(\\+|-)?\\d+(_\\d+)*((\\.\\d+(_\\d+)*)|(?=E))(E(\\+|-)?\\d+(_\\d+)*)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^(\\+|-)?\\d+(_\\d+)*");

    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");


    //Tokenizing State:
    private StringCharacterIterator chars;
    private boolean eof;
    private int currentLine;


    public Tokenizer(Reader reader) throws IOException {
        this.reader = buffer(reader);

        currentLine = 0;
        eof = false;
        nextRawLine();
    }
    private ParsedToken current = null;
    private ParsedToken last = null;
    public Tokenizer.ParsedToken peek() {
        return last = (current == null ? current = next() : current);
    }
    public ParsedToken lastSeen() {
        return last;
    }
    public boolean hasNext() {
        return chars.hasNext() || !eof;
    }
    public Tokenizer.ParsedToken next() {
        if(current == null) {
            //No need to use whitespaces between tokens.
            int lineIndex = currentLine;
            int charIndex = chars.currentIndex();
            try {
                Token token = tryParseNext();
                if(token != null) {
                    return last = new ParsedToken(token, lineIndex, charIndex);
                }
                else {
                    return last = next();
                }
            }
            catch(NoSuchElementException e) {
                throw error("Unexpected end of line", e);
            }
            catch(Exception e) {
                throw error("Unexpected Exception", e);
            }
        }
        else {
            ParsedToken ret = current;
            current = null;
            return last = ret;
        }
    }

    protected boolean matches(SymbolToken symbol, Tokenizer.ParsedToken ptoken) {
        return ptoken.token == symbol;
    }

    public boolean peekIfMatch(SymbolToken symbol) {
        return matches(symbol, peek());
    }
    public boolean nextIfMatch(SymbolToken symbol) {
        if(matches(symbol, peek())) {
            next();
            return true;
        }
        return false;
    }

    ParseException error(String s) {
        return new ParseException(s, currentLine, chars.currentIndex());
    }
    ParseException error(String s,  Exception e) {
        return new ParseException(s, currentLine, chars.currentIndex(), e);
    }

    private void nextRawLine() throws IOException {
        if(eof) {
            throw error("Unexpected end of file");
        }
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
                    c = nextRawChar();
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
                        c = nextRawChar();
                        builder.append(unescaped);
                    }
                    break;
                case '\"':
                    char c2 = nextRawChar();
                    if(c2 == '\"') {
                        char c3 = nextRawChar();
                        if(c3 == '\"') { //END:
                            return ValuedToken.multilineString(builder.toString());
                        }
                        else { //Whoops, not really the end, only ""
                            builder.append(c); //appending c1="
                            builder.append(c2); //appending c2="
                            c = c3; //dealing with c3
                        }
                    }
                    else {
                        builder.append(c); //appending c1="
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
                    return ValuedToken.basicString(builder.toString());

            }
        }
        throw new ParseException("No ending quote for basic string.",currentLine,chars.currentIndex());
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
                throw error("Reserved escaped character '" + escaped + "'");

        }
        try {
            int code = Integer.parseInt(unicode, 16);
            return new String(Character.toChars(code));
        }
        catch(NumberFormatException nfe) {
            throw error("Bad escape code "+unicode);
        }

    }



    private Token parseLiteralString() {
        StringBuilder builder = new StringBuilder();
        while(chars.hasNext()) {
            char c = chars.next();
            if(c == '\'') { //END:
                return ValuedToken.literalString(builder.toString());
            }
            else {
                builder.append(c);
            }
        }
        throw error("No ending single quote for literal string.");
    }
    private Token parseMultilineLiteralString() throws IOException {
        StringBuilder builder = new StringBuilder();
        while(true) {
            if(chars.nextIfSeqEquals("\'\'\'")) { //always in the same line
                return ValuedToken.literalString(builder.toString());
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
        Matcher matcher = DATE_PATTERN.matcher(group);
        matcher.find();
        String datetimeStr = matcher.group(DATE_PATTERN_GROUP_DATETIME);
        String tz = matcher.group(DATE_PATTERN_GROUP_TZ);
        if(tz.equals("Z")) {
            tz = "+0000";  //SimpleDateFormat does not allow Z to be included instead of a timezone, see http://stackoverflow.com/a/2202300/777203
        }
        if(tz.indexOf(':')!= -1) {
            //Again, Java 1.5 SimpleDateFormat is rather limited with its understanding of Timezone formats. 'X' isn't available yet.
            tz = tz.replaceAll(":","");
        }
        String fractionStr = matcher.group(DATE_PATTERN_GROUP_FRACTION);

        SimpleDateFormat parser = new SimpleDateFormat(DATETIME_FORMAT);
        try {
            String fullDate = datetimeStr + tz;
            Date date = parser.parse(fullDate);
            if(fractionStr != null) {
                //So, if we got ".1" that's actually 100,000,000 nanos.
                fractionStr = String.format("%-"+DATE_FRACTION_PERCISION+"s",fractionStr).replace(' ','0');
                long fractional = Integer.parseInt(fractionStr);
                int millies = (int)((fractional*1000)/DATE_FRACTION_MAX);
                date = new Date(date.getTime()+millies);
            }
            return ValuedToken.dateToken(date);
        } catch (java.text.ParseException e) {
            throw error("Couldn't parse date "+group, e);
        }
    }

    private Token parseFloatToken(String group) {
        if(group.charAt(0) == '+') {
            group = group.substring(1);
        }
        group = group.replaceAll("_","");
        return ValuedToken.floatToken(Double.valueOf(group));
    }
    private Token parseIntegerToken(String group) {
        if(group.charAt(0) == '+') {
            group = group.substring(1);
        }
        group = group.replaceAll("_","");
        return ValuedToken.integerToken(Long.valueOf(group));
    }

    private Token tryParseNext() throws IOException {
        if(!chars.hasNext() || chars.nextIfEquals(COMMENT_START)) {
            if(hasNext()) {
                nextRawLine();
            }
            return SymbolToken.Newline; //Always adding a new line for the last line.
        }
        if(Character.isWhitespace(chars.peek())) {
            chars.next();
            return null;
        }


        if(chars.nextIfSeqEquals(MULTILINE_STRING_DELIMITER)) { //Multiline strings start with """
            if(!chars.hasNext()) { //Ignore newline character immediately after """
                nextRawLine();
            }
            return parseMultilineString();
        }

        if(chars.nextIfEquals(BASIC_STRING_DELIMITER)) { //Basic strings starts with just "
            return parseBasicString();
        }
        if(chars.nextIfSeqEquals(LITERAL_MULTILINE_STRING_DELIMITER)) {
            if(!chars.hasNext()) { //Ignore newline character immediately after '''
                nextRawLine();
            }
            return parseMultilineLiteralString();
        }
        if(chars.nextIfEquals(LITERAL_STRING_DELIMITER)) {
            return parseLiteralString();
        }


        if(chars.nextIfSeqEquals("true")) {
            return ValuedToken.booleanToken(true);
        }
        if(chars.nextIfSeqEquals("false")) {
            return ValuedToken.booleanToken(false);
        }

        String match;
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

        char peek = chars.peek();
        SymbolToken symbol = SymbolToken.getSymbolToken(peek);
        if(symbol != null) {
            chars.next();
            return symbol;
        }

        if((match = chars.nextIfMatches(KEY_PATTERN)) != null) {
            return ValuedToken.key(match);
        }

        throw error("Unexpected input: "+chars.peekAll());
    }
}
