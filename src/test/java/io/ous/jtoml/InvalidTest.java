package io.ous.jtoml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Asafh
 * Date: 07/03/15
 * Time: 02:46
 * To change this template use File | Settings | File Templates.
 */
@RunWith(Parameterized.class)
public class InvalidTest {
    private String test;

    public InvalidTest(String test) {
        this.test = test;
    }
    @Parameterized.Parameters
    public static Collection<Object[]> getInvalidTests() {
        //Taken from: https://github.com/BurntSushi/toml-test/tree/master/tests/invalid
        return Utils.createList(
                new Object[] {"no-leads = 1987-7-05T17:45:00Z"},
                new Object[] {"no-secs = 1987-07-05T17:45Z"},
                new Object[] {"no-t = 1987-07-0517:45:00Z"},
                new Object[] {"no-z = 1987-07-05T17:45:00"},
                new Object[] {"with-milli = 1987-07-5T17:45:00.12Z"},
                new Object[] {"[fruit]\n" +
                        "type = \"apple\"\n" +
                        "\n" +
                        "[fruit.type]\n" +
                        "apple = \"yes\""},
                new Object[] {"dupe = false\n" +
                        "dupe = true"},
                new Object[] {"[a]\n" +
                        "[a]"},
                new Object[] {"[naughty..naughty]"},
                new Object[] {"[]"},
                new Object[] {"answer = .12345\n" +
                        "neganswer = -.12345"},
                new Object[] {"answer = 1.\n" +
                        "neganswer = -1."},
                new Object[] {"= 1"},
                new Object[] {"a# = 1"},
                new Object[] {"a\n" +
                        "= 1"},
                new Object[] {"[abc = 1"},
                new Object[] {"["},
                new Object[] {"a b = 1"},
                new Object[] {"[a]\n" +
                        "[xyz = 5\n" +
                        "[b]"},
                new Object[] {"key= = 1"},
                new Object[] {"naughty = \"\\xAg\""},
                new Object[] {"no-ending-quote = \"One time, at band camp"},
                new Object[] {"invalid-escape = \"This string has a bad \\a escape character.\""},
                new Object[] {"answer = \"\\x33\""},
                new Object[] {"[[albums.songs]]\n" +
                        "name = \"Glory Days\"\n" +
                        "\n" +
                        "[[albums]]\n" +
                        "name = \"Born in the USA\""},
                new Object[] {"[[albums]\n" +
                        "name = \"Born to Run\""},
                new Object[] {"[[]]\n" +
                        "name = \"Born to Run\""},
                new Object[] {"[]"},
                new Object[] {"[a]b]\n" +
                        "zyx = 42"},
                new Object[] {"[a[b]\n" +
                        "zyx = 42"},
                new Object[] {"[invalid key]"},
                new Object[] {"[key#group]\n" +
                        "answer = 42"},
                new Object[] {"array = [\n" +
                        "  \"Is there life after an array separator?\", No\n" +
                        "  \"Entry\"\n" +
                        "]"},
                new Object[] {"answer = 42 the ultimate answer?"},
                new Object[] {"string = \"Is there life after strings?\" No."},
                new Object[] {"[error] this shouldn't be here"},
                new Object[] {"array = [\n" +
                        "  \"Is there life before an array separator?\" No,\n" +
                        "  \"Entry\"\n" +
                        "]"},
                new Object[] {"array = [\n" +
                        "  \"Entry 1\",\n" +
                        "  I don't belong,\n" +
                        "  \"Entry 2\",\n" +
                        "]"}

        );
    }

    @Test(expected = ParseException.class)
    public void test() {
        JToml.parseString(test);
    }
}
