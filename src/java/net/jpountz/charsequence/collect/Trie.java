package net.jpountz.charsequence.collect;

import it.unimi.dsi.fastutil.chars.CharCollection;

/**
 * A trie. http://en.wikipedia.org/wiki/Trie.
 *
 * Unless otherwise specified, instances of this class:
 *  - are not thread-safe,
 *  - do not support keys containing '\0',
 *  - do not support null values.
 *
 * @param <T> the value type
 */
public interface Trie<T> extends CharSequenceMap<T> {

	/**
	 * A strategy to traverse a trie.
	 */
	public interface Traversal {

		/**
		 * Traversing of the trie.
		 *
		 * @param <T> the node type
		 * @param node the root node for traversing
		 * @param cursor the cursor to move
		 * @return false if all nodes have been traversed
		 */
		<T> boolean moveToNextNode(Trie.Node node, Trie.Cursor<T> cursor);

		public static final Traversal DEPTH_FIRST = new Traversal() {
			public <T> boolean moveToNextNode(Trie.Node node, Trie.Cursor<T> cursor) {
				if (cursor.moveToFirstChild() || cursor.moveToBrother()) {
					return true;
				} else {
					if (cursor.isAt(node)) {
						return false;
					} else {
						while (cursor.moveToParent()) {
							if (cursor.isAt(node)) {
								return false;
							} else if (cursor.moveToBrother()) {
								return true;
							}
						}
						throw new IllegalStateException();
					}
				}
			}
		};

		public static final Traversal BREADTH_FIRST = new Traversal() {
			public <T> boolean moveToNextNode(Trie.Node node, Trie.Cursor<T> cursor) {
				if (cursor.moveToBrother()) {
					return true;
				} else {
					int depth = cursor.depth();
					int newDepth = depth;
					while (true) {
						if (cursor.isAt(node)) {
							// go to the first node at depth + 1
							++depth;
							if (cursor.moveToFirstChild()) {
								if (depth == cursor.depth()) {
									return true;
								}
							} else {
								return false; // nothing under node
							}
							while (true) {
								if (cursor.moveToFirstChild()) {
									if (depth == cursor.depth()) {
										return true;
									}
								} else if (!cursor.moveToBrother()) {
									while (cursor.moveToParent()) {
										if (cursor.isAt(node)) {
											return false;
										} else if (cursor.moveToBrother()) {
											break;
										}
									}
								}
							}
						}
						if (cursor.moveToParent()) {
							--newDepth;
						}
						// go to the next node at depth
						while (cursor.moveToBrother()) {
							int previousDepth = newDepth;
							while (cursor.moveToFirstChild()) {
								++newDepth;
								if (newDepth == depth) {
									return true;
								}
							}
							for (; newDepth > previousDepth+1; --newDepth) {
								cursor.moveToParent();
							}
						}
					}
				}
			}
		};

		public static final Traversal BREADTH_FIRST_THEN_DEPTH = new Traversal() {
			public <T> boolean moveToNextNode(Trie.Node node, Trie.Cursor<T> cursor) {
				if (cursor.moveToBrother()) {
					return true;
				} else {
					if (cursor.moveToParent()) {
						if (!cursor.moveToFirstChild()) {
							return false;
						}
					}
					if (cursor.moveToFirstChild()) {
						return true;
					} else {
						while (true) {
							while (cursor.moveToBrother()) {
								if (cursor.moveToFirstChild()) {
									return true;
								}
							}
							cursor.moveToParent();
							if (cursor.isAt(node)) {
								return false;
							}
						}
					}
				}
			}
		};
	}

	/**
	 * Indicates that the object is optimizable for a certain type
	 * of traversal.
	 */
	public interface Optimizable {
		/**
		 * Optimize for a given type of traversal.
		 *
		 * Classes that implement this interface will alter the trie so that
		 * traversal will be faster. This is usually done by putting close to
		 * each other array slices that are likely to be read one after
		 * another.
		 *
		 * @param traversal
		 */
		void optimizeFor(Traversal traversal);
	}

	/**
	 * Indicates that this object allocates more memory than it needs upon
	 * insertion in order to reduce the memory allocation overhead.
	 */
	public interface Trimmable {
		/**
		 * Set the capacity of this object to be equal to its size.
		 */
		void trimToSize();
	}

	/**
	 * Indicates that this object can be compiled to a read-only copy that
	 * is likely to provide better performance for read-only operations.
	 *
	 * @param <T> the value type
	 */
	public interface Compilable<T> {
		/**
		 * Compile this object.
		 *
		 * @return a compiled copy of the trie
		 */
		Trie<T> compile();
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
	public interface Cursor<T> {

		/**
		 * Get the node corresponding to the position of the cursor.
		 *
		 * @return
		 */
		Node getNode();

		/**
		 * Get whether the cursor is at the root node.
		 *
		 * @return true if, and only if the cursor is at the root node
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
		 * Remove all the node's children.
		 */
		void removeChildren();

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

	}

	/**
	 * Get a cursor on this trie.
	 *
	 * @return
	 */
	Cursor<T> getCursor();

	/**
	 * Get the number of nodes of the trie.
	 *
	 * @return
	 */
	int nodes();

}
