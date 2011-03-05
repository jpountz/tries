package net.jpountz.trie;

/**
 * A Radix trie. On the contrary to tries, edges' labels can have a length > 1.
 *
 * @param <T> the value type.
 */
public interface RadixTrie<T> extends Trie<T> {

	/**
	 * Indicate that a substantial amount of memory can be saved by interning
	 * the labels of the edges of this radix trie.
	 */
	public interface LabelsInternable {
		public void internLabels(TrieFactory<char[]> labels);
		public void internLabels(Trie<char[]> trie);
	}

	/**
	 * Indicates that this object can be compiled to a read-only copy that
	 * is likely to provide better performance for read-only operations.
	 *
	 * @param <T> the value type
	 */
	public interface Compilable<T> extends Trie.Compilable<T> {
		/**
		 * Compile this object.
		 *
		 * @return a compiled copy of the trie
		 */
		RadixTrie<T> compile();
	}

	/**
	 * A cursor on a Radix trie.
	 *
	 * @param <T> the value type
	 */
	public interface Cursor<T> extends Trie.Cursor<T> {

		/**
		 * Move to the first child in the radix trie.
		 *
		 * @return true if, and only if there is such a child
		 */
		boolean moveToFirstChildRadix();

		/**
		 * Move to the next brother in the radix trie.
		 *
		 * @return true if, and only if there is such a brother
		 */
		//boolean moveToFirstBrotherRadix();

		/**
		 * Move to the parent in the radix trie.
		 *
		 * @return true if, and only if the cursor is not at the root node
		 */
		boolean moveToParentRadix();

		/**
		 * Create a new child and move to it.
		 *
		 * @param buffer
		 * @param offset
		 * @param length
		 */
		void addChild(char[] buffer, int offset, int length);

		/**
		 * Create a new child and move to it.
		 *
		 * @param sequence
		 * @param offset
		 * @param length
		 */
		void addChild(CharSequence sequence, int offset, int length);

		/**
		 * Return the number of nodes under this cursor position. The value
		 * is usually lower than the size of a regular trie having the same
		 * content.
		 */
		public int radixSize();
	}

	/**
	 * Return a cursor on the radix trie.
	 */
	Cursor<T> getCursor();

	/**
	 * Return the number of nodes in the radix trie. The value is usually
	 * lower than the size of a regular trie having the same content.
	 */
	public int radixSize();

}
