package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharCollection;

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

	public interface Optimizable {
		void optimizeFor(TrieTraversal traversal);
	}

	/**
	 * An opaque reference to a node of the trie.
	 */
	public interface Node {}

	/**
	 * A cursor to move inside a trie.
	 *
	 * @param <T> the values type
	 */
	public interface Cursor<T> extends Cloneable {

		/**
		 * Get the node corresponding to the position of the cursor.
		 *
		 * @return
		 */
		Node getNode();

		/**
		 * Return the first child.
		 *
		 * @return
		 */
		Node getFirstChildNode();

		/**
		 * Get the label leading to the first child node.
		 *
		 * @return the char or '\0' if there is no first child
		 */
		char getFirstEdgeLabel();

		/**
		 * Get whether there are children under this.
		 *
		 * @return
		 */
		boolean hasChildren();

		/**
		 * Return the first brother.
		 *
		 * @return the brother node
		 */
		Node getBrotherNode();

		/**
		 * Get the label leading to the first brother of the node.
		 *
		 * @return the char or '\0' if there is no brother
		 */
		char getBrotherEdgeLabel();

		/**
		 * Get whether there is a brother.
		 *
		 * @return
		 */
		boolean hasBrother();

		/**
		 * Get the children of this node.
		 *
		 * @param children
		 */
		void getChildren(Char2ObjectMap<Node> children);

		/**
		 * Get whether the cursor is at root.
		 *
		 * @return true if, and only if the cursor is at root
		 */
		boolean isAtRoot();

		/**
		 * Get whether the cursor is at node.
		 *
		 * @param node the node to test
		 * @return true if, and only if the cursor is at node
		 */
		boolean isAt(Node node);

		/**
		 * Get the depth corresponding to the position of the cursor.
		 *
		 * @return the depth
		 */
		int depth();

		/**
		 * Get the label corresponding to the cursor position.
		 *
		 * @return the label
		 */
		String getLabel();

		/**
		 * Get the label of the edge leading to the current node or '\0' if
		 * this node is the root node.
		 *
		 * @return the label of the edge leading to the current node.
		 */
		char getEdgeLabel();

		/**
		 * Move to a child node.
		 *
		 * @param c the child label
		 * @return true if, and only if there is such a node
		 */
		boolean moveToChild(char c);

		/**
		 * Move to the first child of the cursor.
		 *
		 * @return true if, and only if there is such a child
		 */
		boolean moveToFirstChild();

		/**
		 * Move to the first brother of the cursor.
		 *
		 * @return trie if, and only if, the node has a brother
		 */
		boolean moveToBrother();

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
		boolean removeChild(char c);

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
		void getChildrenLabels(CharCollection children);

		/**
		 * Get the number of children.
		 *
		 * @return the number of children
		 */
		int getChildrenSize();

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
