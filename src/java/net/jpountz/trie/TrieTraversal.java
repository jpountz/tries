package net.jpountz.trie;

/**
 * A strategy to traverse a trie.
 */
public interface TrieTraversal {

	/**
	 * Traversing of the trie.
	 *
	 * @param <T> the node type
	 * @param node the root node for traversing
	 * @param cursor the cursor to move
	 * @return false if all nodes have been traversed
	 */
	<T> boolean moveToNextNode(Trie.Node node, Trie.Cursor<T> cursor);

	public static final TrieTraversal DEPTH_FIRST = new TrieTraversal() {
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

	public static final TrieTraversal BREADTH_FIRST = new TrieTraversal() {
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

	public static final TrieTraversal BREADTH_FIRST_THEN_DEPTH = new TrieTraversal() {
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
