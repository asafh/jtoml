package io.ous.jtoml;

import java.util.HashMap;
import java.util.Map;

import io.ous.jtoml.impl.KeyGroup;
import io.ous.jtoml.impl.Path;
import io.ous.jtoml.impl.TomlVisitor;

/**
 * The root Keygroup, a Toml configuration file
 * @author Asafh
 */
public class Toml extends KeyGroup {
	public Toml() {
		super(null, "");
	}
}
