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
	
	
	/**
	 * Returns an exploded map of this Toml.
	 * this is very memory intensive: it creates a map for every key group, containing all of the values
	 * of it's keygroups under it flat ("b.c" :val) and a map for any keygroup under it ("b": { c: val}). <br/>
	 * If you need this sort of accessibilty, juse use Toml's getX methods rather than exploding the values into a map. <br/>
	 * <i>This was only done for playing around, and isn't really needed</i> 
	 * @return
	 */
	public Map<String,Object> toExplodedMap() {
		final Map<String, Object> map = new HashMap<String, Object>();
		this.traverse(new TomlVisitor() {
			@SuppressWarnings("unchecked")
			private void setValue(KeyGroup in, String localName, Object value) {
				String fullpath = Path.join(in.getPath(),localName);
				int delimIndex = -1;
				while(true) {
					delimIndex = fullpath.indexOf(Path.KEYGROUP_DELIMITER, delimIndex+1);
					if(delimIndex == -1) {
						map.put(fullpath,value);
						break;
					}
					else {
						String groupKey = fullpath.substring(0,delimIndex);
						String valueKey = fullpath.substring(delimIndex+1);
						((Map<String, Object>)map.get(groupKey)).put(valueKey, value);
					}
				}
			}
			public void visitLeaf(KeyGroup in, String localName, Object value) {
				setValue(in, localName, value);
			}

			public void visitPreorderKeygroup(KeyGroup group) {
				if(group != Toml.this) {
					map.put(group.getPath(), new HashMap<String, Object>());
				}
			}

			public void visitPostorderKeygroup(KeyGroup group) {
				
			}
		});
		return map;
	}
	
	/**
	 * Returns a map of this Toml. <br/>
	 * Calling {@link Map#get(Object)} for a key representing a value will return it and for a key representing a key group will return it's KeyGroup instance
	 * @return
	 */
	public Map<String,Object> toMap() {
		final Map<String, Object> map = new HashMap<String, Object>();
		this.traverse(new TomlVisitor() {
			private void setValue(KeyGroup in, String relativePath, Object value) {
				String fullpath = Path.join(in.getPath(),relativePath);
				map.put(fullpath, value);
			}
			public void visitLeaf(KeyGroup in, String localName, Object value) {
				setValue(in, localName, value);
			}

			public void visitPreorderKeygroup(KeyGroup group) {
				if(group != Toml.this) {
					setValue(Toml.this, group.getPath(), group);
				}
			}

			public void visitPostorderKeygroup(KeyGroup group) {
				
			}
		});
		return map;
	}
}
