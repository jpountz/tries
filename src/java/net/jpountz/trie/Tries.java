package net.jpountz.trie;

import java.nio.CharBuffer;
import java.util.Set;


/**
 * Algorithms performed on tries.
 */
public class Tries {

	private Tries() {}

	/**
	 * DFS traversing of the trie.
	 *
	 * @param <T> the node type
	 * @param node the root node for traversing
	 * @param cursor the cursor to move
	 * @return false if all nodes have been traversed
	 */
	public static <T> boolean moveToNextNode(Trie.Node node, Trie.Cursor<T> cursor) {
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



	/**
	 * Move to the next suffix lexicographically.
	 *
	 * @param <T> the value type
	 * @param under node to look for suffixes under
	 * @param cursor the cursor to move
	 * @return false if there is no more suffix
	 */
	public static <T> boolean moveToNextSuffix(Trie.Node under, Trie.Cursor<T> cursor) {
		while (moveToNextNode(under, cursor)) {
			if (cursor.getValue() != null) {
				return true;
			}
		}
		return false;
	}

	public static <T> void getNeighbors(CharSequence sequence,
			Trie<T> trie, int distance, Set<Trie.Entry<T>> neighbors) {
		getNeighborsR(sequence, 0, sequence.length(),
				trie.getCursor(), distance, neighbors);
	}

	public static <T> void getNeighbors(char[] buffer, int offset, int length,
			Trie<T> trie, int distance, Set<Trie.Entry<T>> neighbors) {
		CharBuffer sequence = CharBuffer.wrap(buffer, offset, length);
		getNeighborsR(sequence, 0, sequence.length(),
				trie.getCursor(), distance, neighbors);
	}

	public static <T> void getNeighbors(char[] buffer,
			Trie<T> trie, int distance, Set<Trie.Entry<T>> neighbors) {
		getNeighbors(buffer, 0, buffer.length, trie, distance, neighbors);
	}

	private static <T> void getNeighborsR(CharSequence sequence, int offset, int length,
			Trie.Cursor<T> cursor, final int distance, Set<Trie.Entry<T>> neighbors) {
		if (distance == 0) {
			int n = 0;
			for (int i = 0; i < length; ++i) {
				if (cursor.moveToChild(sequence.charAt(offset+i))) {
					++n;
				} else {
					for (int k = 0; k < n; ++k) {
						cursor.moveToParent();
					}
					return;
				}
			}
			T value = cursor.getValue();
			if (value != null) {
				neighbors.add(new AbstractTrie.EntryImpl<T>(cursor.getLabel(), value));
			}
			for (int i = 0; i < length; ++i) {
				cursor.moveToParent();
			}
		} else if (length == 0) {
			T value = cursor.getValue();
			if (value != null) {
				neighbors.add(new AbstractTrie.EntryImpl<T>(cursor.getLabel(), value));
			}
			if (cursor.moveToFirstChild()) {
				int newDistance = distance - 1;
				getNeighborsR(sequence, offset, length, cursor, newDistance, neighbors);
				while (cursor.moveToBrother()) {
					getNeighborsR(sequence, offset, length, cursor, newDistance, neighbors);
				}
				cursor.moveToParent();
			}
		} else {
			if (length <= distance) {
				T value = cursor.getValue();
				if (value != null) {
					neighbors.add(new AbstractTrie.EntryImpl<T>(cursor.getLabel(), value));
				}
			}

			if (cursor.moveToFirstChild()) {
				if (distance > 0) {
					getNeighborsR(sequence, offset, length, cursor, distance - 1, neighbors);
				}
				char edgeLabel = cursor.getEdgeLabel();
				int newDistance = distance;
				if (edgeLabel != sequence.charAt(offset)) {
					--newDistance;
				}
				++offset;
				--length;
				int max = Math.min(newDistance, length);
				for (int i = 0; i <= max; ++i) {
					getNeighborsR(sequence, offset + i, length - i, cursor, newDistance - i, neighbors);
				}

				while (cursor.moveToBrother()) {
					if (distance > 0) {
						getNeighborsR(sequence, offset, length, cursor, distance - 1, neighbors);
					}
					char brotherLabel = cursor.getEdgeLabel();
					if (edgeLabel == sequence.charAt(offset - 1)) {
						--newDistance;
					} else if (brotherLabel == sequence.charAt(offset - 1)) {
						++newDistance;
					}
					max = Math.min(newDistance, length);
					for (int i = 0; i <= max; ++i) {
						getNeighborsR(sequence, offset + i, length - i, cursor, newDistance - i, neighbors);
					}
					edgeLabel = brotherLabel;
				}

				cursor.moveToParent();
			}
		}
	}

/*	public static <T> void getNeighbors(char[] buffer, int offset, int length,
			Trie.Cursor<T> cursor, int distance, Collection<Trie.Entry<T>> neighbors) {
		cursor.reset();
		while (true) {
			// 1. Move
			if (cursor.moveToFirstChild()) {
				if (length <= 0 || buffer[offset] != cursor.getEdgeLabel()) {
					--distance;
				}
				++offset;
				--length;
			} else  {
				char previousLabel = cursor.getEdgeLabel();
				if (cursor.moveToBrother()) { 
					if (length >= 0) {
						if (previousLabel == buffer[offset - 1]) {
							--distance;
						} else {
							char currentLabel = cursor.getEdgeLabel();
							if (currentLabel == buffer[offset - 1]) {
								++distance;
							}
						}
					} // else no change to distance
				} else {
					char childLabel = cursor.getEdgeLabel();
					while (true) {
						if (!cursor.moveToParent()) {
							return;
						}
						if (length > 0 && childLabel != buffer[offset]) {
							++distance;
						}
						--offset;
						++length;
						childLabel = cursor.getEdgeLabel();
						if (cursor.moveToBrother()) {
							if (length >= 0) {
								if (previousLabel == buffer[offset - 1]) {
									--distance;
								} else {
									char currentLabel = cursor.getEdgeLabel();
									if (currentLabel == buffer[offset - 1]) {
										++distance;
									}
								}
							} // else no change to distance
							break;
						}
					}
				}
			}

			// 2. Check
			if (Math.abs(length) <= distance) {
				T value = cursor.getValue();
				if (value != null) {
					neighbors.add(new AbstractTrie.EntryImpl<T>(cursor.getLabel(), value));
				}
			}
		}
	}*/

}
