package net.jpountz.charsequence.collect;

import java.nio.CharBuffer;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jpountz.charsequence.CharComparator;


/**
 * Algorithms performed on tries.
 */
public enum Tries {
	;

	/**
	 * Move to the next suffix in the order of traversal.
	 *
	 * @param <T> the value type
	 * @param under node to look for suffixes under
	 * @param cursor the cursor to move
	 * @return false if there is no more suffix
	 */
	public static <T> boolean moveToNextSuffix(Trie.Node under,
			Trie.Cursor<T> cursor, Trie.Traversal traversal) {
		while (traversal.moveToNextNode(under, cursor)) {
			if (cursor.getValue() != null) {
				return true;
			}
		}
		return false;
	}

	public static <T> void getNeighbors(CharSequence sequence,
			Trie<T> trie, int distance, Set<Map.Entry<String, T>> neighbors) {
		getNeighborsR(sequence, 0, sequence.length(),
				trie.getCursor(), distance, neighbors);
	}

	public static <T> void getNeighbors(char[] buffer, int offset, int length,
			Trie<T> trie, int distance, Set<Map.Entry<String, T>> neighbors) {
		CharBuffer sequence = CharBuffer.wrap(buffer, offset, length);
		getNeighborsR(sequence, 0, sequence.length(),
				trie.getCursor(), distance, neighbors);
	}

	public static <T> void getNeighbors(char[] buffer,
			Trie<T> trie, int distance, Set<Map.Entry<String, T>> neighbors) {
		getNeighbors(buffer, 0, buffer.length, trie, distance, neighbors);
	}

	private static <T> void getNeighborsR(CharSequence sequence, int offset, int length,
			Trie.Cursor<T> cursor, final int distance, Set<Map.Entry<String, T>> neighbors) {
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
				neighbors.add(new AbstractMap.SimpleImmutableEntry<String, T>(cursor.getLabel(), value));
			}
			for (int i = 0; i < length; ++i) {
				cursor.moveToParent();
			}
		} else if (length == 0) {
			T value = cursor.getValue();
			if (value != null) {
				neighbors.add(new AbstractMap.SimpleImmutableEntry<String, T>(cursor.getLabel(), value));
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
					neighbors.add(new AbstractMap.SimpleImmutableEntry<String, T>(cursor.getLabel(), value));
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

	public static <T> Trie<T> sortedCharArrayListAsTrie(
			List<char[]> keys, List<T> values, CharComparator comparator) {
		return new CharArrayBinarySearchTrie<T>(keys, values, comparator);
	}

	public static <T> Trie<T> sortedCharArrayListAsTrie(List<char[]> keys, List<T> values) {
		return sortedCharArrayListAsTrie(keys, values, null);
	}

	public static <K extends CharSequence, T> Trie<T> sortedCharSequenceListAsTrie(
			List<K> keys, List<T> values, CharComparator comparator) {
		return new CharSequenceBinarySearchTrie<K, T>(keys, values, comparator);
	}

	public static <K extends CharSequence, T> Trie<T> sortedCharSequenceListAsTrie(
			List<K> keys, List<T> values) {
		return sortedCharSequenceListAsTrie(keys, values, null);
	}

}
