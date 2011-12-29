package net.jpountz.charsequence.collect;

import java.util.List;

import net.jpountz.charsequence.CharComparator;

/**
 * Adapdation of {@link AbstractBinarySearchTrie} to handle char[] keys.
 */
class CharSequenceBinarySearchTrie<K extends CharSequence, T> extends AbstractBinarySearchTrie<K, T> {

	/**
	 * @param keys the keys, sorted lexicographically
	 * @param values the values
	 */
	public CharSequenceBinarySearchTrie(List<K>keys, List<T> values, CharComparator comparator) {
		super(keys, values, comparator);
	}

	@Override
	protected int compare(
			char[] key1, int off1, int len1,
			CharSequence key2) {
		return comparator.compare(key1, off1, len1, key2, 0, key2.length());
	}

	@Override
	protected int compare(
			CharSequence key1, int off1, int len1,
			CharSequence key2) {
		return comparator.compare(key1, off1, len1, key2, 0, key2.length());
	}

	@Override
	protected int size(CharSequence key) {
		return key.length();
	}

	@Override
	protected char charAt(CharSequence key, int offset) {
		return key.charAt(offset);
	}

	protected String toString(K key) {
		return key.toString();
	}
}
