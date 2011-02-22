package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;

/**
 * A trie. http://en.wikipedia.org/wiki/Trie
 *
 * @param <T> the value type
 */
public interface Trie<T> {

	/**
	 * An entry of the trie.
	 *
	 * @param <T> the value type
	 */
	public interface Entry<T> {

		/**
		 * The entry key.
		 *
		 * @return the ley
		 */
		CharSequence getKey();

		/**
		 * The entry value.
		 *
		 * @return the value
		 */
		T getValue();
	}

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
		void getChildrenLabels(CharArrayList children);

		/**
		 * Get the children of the current node.
		 *
		 * @param children a map to fill
		 */
		void getChildren(Char2ObjectMap<Cursor<T>> children);

		/**
		 * Get all the available suffixes.
		 *
		 * @return the suffixes
		 */
		Iterable<Entry<T>> getSuffixes();

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

	Cursor<T> getCursor();

	void clear();

	void put(char[] buffer, int offset, int length, T value);
	void put(char[] buffer, T value);
	void put(CharSequence sequence, int offset, int length, T value);
	void put(CharSequence sequence, T value);

	void remove(char[] buffer, int offset, int length);
	void remove(char[] buffer);
	void remove(CharSequence sequence, int offset, int length);
	void remove(CharSequence sequence);

	T get(char[] buffer, int offset, int length);
	T get(char[] buffer);
	T get(CharSequence sequence, int offset, int length);
	T get(CharSequence sequence);

	int size();
	void trimToSize();
}
