package io.ous.jtoml.impl;

public interface TomlTraverser {
	public void visitPreorderKeygroup(KeyGroup group);
	public void visitLeaf(KeyGroup in, String localName, Object value);
	public void visitPostorderKeygroup(KeyGroup group);
}
