package io.ous.jtoml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;


/**
 * Unit test for simple App.
 */
public class AdhocTest {
    @Test
    public void testApp() throws FileNotFoundException, IOException {
        Toml toml = JToml.parse(new File("D:\\Workspaces\\config\\jtoml\\src\\test\\resources\\example.toml"));
        System.out.println(toml);
        System.out.println("=====================================");
        Map<String, Object> map = toml.toExplodedMap();
		System.out.println(map);
		System.out.println("=====================================");
		map = toml.toMap();
		System.out.println(map);
    }
}
