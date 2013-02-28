package io.ous.jtoml;

import io.ous.jtoml.impl.Parser;
import io.ous.jtoml.impl.Toml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.Test;


/**
 * Unit test for simple App.
 */
public class AdhocTest {
    @Test
    public void testApp() throws FileNotFoundException, IOException {
        Toml toml = new Parser(new File("D:\\Workspaces\\config\\jtoml\\src\\test\\resources\\example.toml")).parse();
        System.out.println(toml);
        System.out.println("=====================================");
        Map<String, Object> map = toml.toMap();
		System.out.println(map);
    }
}
