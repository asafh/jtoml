package io.ous.jtoml.impl;

public interface TomlVisitor {
	/**
	 * Called on a Keygroup <b>before</b> visiting all nested keys (and keygroups)
	 * @param group
	 */
	public void visitPreorderKeygroup(KeyGroup group);
	/**
	 * Called on every non-keygroup node
	 * @param in
	 * @param localName
	 * @param value
	 */
	public void visitLeaf(KeyGroup in, String localName, Object value);
	/**
	 * Called on a Keygroup <b>after</b> visiting all nested keys (and keygroups)
	 * @param group
	 */
	public void visitPostorderKeygroup(KeyGroup group);
}
