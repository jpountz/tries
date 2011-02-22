package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharArrayList;

/**
 * A cursor to move inside a trie.
 *
 * @param <T> the values type
 */
public interface Cursor<T> {

	/**
	 * Move to a child node.
	 *
	 * @param c the child label
	 * @return true if, and only if there is such a node
	 */
	boolean moveToChild(char c);

	/**
	 * Create a new child and move to it.
	 *
	 * @param c the child label
	 */
	void addChild(char c);

	/**
	 * Remove a child node.
	 *
	 * @param c the child label
	 */
	void removeChild(char c);

	/**
	 * Move to the parent node.
	 *
	 * @return true if, and only if the node is not the root node
	 */
	boolean moveToParent();

	/**
	 * Get the labels of the children of the current node.
	 *
	 * @param children
	 */
	void getChildren(CharArrayList children);

	/**
	 * Get the value of the node.
	 *
	 * @return the node value
	 */
	T getValue();

	/**
	 * Set the value of the current node.
	 *
	 * @param value the new node value
	 */
	void setValue(T value);

	/**
	 * Move to the root node.
	 */
	void reset();

	/**
	 * Get the number of nodes under this (included).
	 *
	 * @return the number of node
	 */
	int size();

	/**
	 * Get a copy of this cursor.
	 *
	 * @return a copy of this cursor
	 */
	Cursor<T> clone();
}
