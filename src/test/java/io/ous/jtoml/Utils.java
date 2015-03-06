package io.ous.jtoml;

import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    static Date createCalendar(String value)
            throws ParseException, java.text.ParseException {
        return Utils.createCalendar("yyyy-MM-dd-HH:mm:ssZ", value);
    }
    static Date createCalendar(String pattern, String value)
            throws ParseException, java.text.ParseException {
        return new SimpleDateFormat(pattern).parse(value);
    }


    static <T> List<T> createList(T... elements) {
        return Arrays.<T> asList(elements);
    }
}
