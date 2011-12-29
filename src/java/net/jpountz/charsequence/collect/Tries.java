package net.jpountz.charsequence.collect;

import java.nio.CharBuffer;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jpountz.charsequence.BasicEditWeight;
import net.jpountz.charsequence.CharComparator;
import net.jpountz.charsequence.EditWeight;


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
			Trie<T> trie, EditWeight weight, double distance, Set<Map.Entry<String, T>> neighbors) {
		if (weight instanceof BasicEditWeight) {
			// optimisations, twice as fast with distance=4 on an english dictionary
			getNeighborsR(sequence, 0, sequence.length(),
					trie.getCursor(), (BasicEditWeight) weight, distance, neighbors);
		} else {
			getNeighborsR(sequence, 0, sequence.length(), 0, sequence.length(),
					trie.getCursor(), weight, distance, neighbors);
		}
	}

	public static <T> void getNeighbors(char[] buffer, int offset, int length,
			Trie<T> trie, EditWeight weight, double distance, Set<Map.Entry<String, T>> neighbors) {
		CharBuffer sequence = CharBuffer.wrap(buffer, offset, length);
		getNeighbors(sequence, trie, weight, distance, neighbors);
	}

	public static <T> void getNeighbors(char[] buffer,
			Trie<T> trie, EditWeight weight, double distance, Set<Map.Entry<String, T>> neighbors) {
		getNeighbors(buffer, 0, buffer.length, trie, weight, distance, neighbors);
	}

	private static <T> void getNeighborsR(CharSequence sequence, int offset, int length,
			Trie.Cursor<T> cursor, BasicEditWeight weight, final double distance, Set<Map.Entry<String, T>> neighbors) {

		if (distance < weight.insertionCost() &&
				distance < weight.deletionCost() &&
				distance < weight.transpositionCost() &&
				distance < weight.substitutionCost()) {

			// Just follow the path
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

		}

		else if (length <= 0) {

			// First check whether there is a match
			T value = cursor.getValue();
			if (value != null) {
				neighbors.add(new AbstractMap.SimpleImmutableEntry<String, T>(cursor.getLabel(), value));
			}

			if (cursor.moveToFirstChild()) {

				// Add suffixes while distance > 0 (insertions at the end)
				double newDistance = distance - weight.insertionCost();

				do {
					getNeighborsR(sequence, offset, length, cursor, weight, newDistance, neighbors);
				} while (cursor.moveToBrother());

				cursor.moveToParent();

			}

		}

		else  { // length > 0

			double deletionDistance = distance - weight.deletionCost();
			if (deletionDistance >= 0) {

				getNeighborsR(sequence, offset+1, length-1, cursor,
						weight, deletionDistance, neighbors);
			}

			if (cursor.moveToFirstChild()) {
				double insertionDistance = distance - weight.insertionCost();
				double substitutionDistance = distance - weight.substitutionCost();
				double transpositionDistance = distance - weight.transpositionCost();

				do {

					if (insertionDistance >= 0) { // insertion
						getNeighborsR(sequence, offset, length, cursor, weight, insertionDistance, neighbors);
					}

					if (cursor.getEdgeLabel() == sequence.charAt(offset)) { // match
						getNeighborsR(sequence, offset+1, length-1, cursor, weight, distance, neighbors);
					}

					else {

						if (weight.substitutionEnabled() && substitutionDistance >= 0) { // substitution
							getNeighborsR(sequence, offset+1, length-1, cursor, weight, substitutionDistance, neighbors);
						}

						if (weight.transpositionEnabled() &&
								transpositionDistance >= 0 &&
								length >= 2 &&
								cursor.getEdgeLabel() == sequence.charAt(offset+1) &&
								cursor.moveToChild(sequence.charAt(offset))) { // transposition
							getNeighborsR(sequence, offset+2, length-2, cursor, weight, transpositionDistance, neighbors);
							cursor.moveToParent();
						}
					}

				} while (cursor.moveToBrother());
				cursor.moveToParent();
			}

		}
	}

	private static <T> void getNeighborsR(CharSequence sequence,
			int originalOffset, int originalLength,
			int offset, int length,
			Trie.Cursor<T> cursor, EditWeight weight, final double distance, Set<Map.Entry<String, T>> neighbors) {

		if (length <= 0) {

			// First check whether there is a match
			T value = cursor.getValue();
			if (value != null) {
				neighbors.add(new AbstractMap.SimpleImmutableEntry<String, T>(cursor.getLabel(), value));
			}

			if (cursor.moveToFirstChild()) {

				// Add suffixes while distance > 0 (insertions at the end)
				do {
					double newDistance = distance - weight.insertionCost(cursor.depth()-1, cursor.getEdgeLabel());
					if (newDistance >= 0) {
						getNeighborsR(sequence, originalOffset, originalLength,
								offset, length, cursor, weight, newDistance, neighbors);
					}
				} while (cursor.moveToBrother());

				cursor.moveToParent();

			}

		}

		else  { // length > 0

			double deletionDistance = distance - weight.deletionCost(offset - originalOffset,
					sequence.charAt(offset));
			if (deletionDistance >= 0) {

				getNeighborsR(sequence,
						originalOffset, originalLength,
						offset+1, length-1, cursor,
						weight, deletionDistance, neighbors);
			}

			if (cursor.moveToFirstChild()) {

				do {

					double insertionDistance = distance - weight.insertionCost(offset-originalLength, cursor.getEdgeLabel());
					if (insertionDistance >= 0) { // insertion
						getNeighborsR(sequence,
								originalOffset, originalLength,
								offset, length,
								cursor, weight, insertionDistance, neighbors);
					}

					if (cursor.getEdgeLabel() == sequence.charAt(offset)) { // match
						getNeighborsR(sequence,
								originalOffset, originalLength,
								offset+1, length-1,
								cursor, weight, distance, neighbors);
					}

					else {

						double substitutionDistance = distance - weight.substitutionCost(
								offset-originalLength, cursor.depth()-1, sequence.charAt(offset), cursor.getEdgeLabel());
						if (weight.substitutionEnabled() && substitutionDistance >= 0) { // substitution
							getNeighborsR(sequence,
									originalOffset, originalLength,
									offset+1, length-1,
									cursor, weight, substitutionDistance, neighbors);
						}

						double transpositionDistance = distance - weight.substitutionCost(
								offset-originalLength, cursor.depth()-1, sequence.charAt(offset), cursor.getEdgeLabel());
						if (weight.transpositionEnabled() &&
								transpositionDistance >= 0 &&
								length >= 2 &&
								cursor.getEdgeLabel() == sequence.charAt(offset+1) &&
								cursor.moveToChild(sequence.charAt(offset))) { // transposition
							getNeighborsR(sequence,
									originalOffset, originalLength,
									offset+2, length-2,
									cursor, weight, transpositionDistance, neighbors);
							cursor.moveToParent();
						}
					}

				} while (cursor.moveToBrother());
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
