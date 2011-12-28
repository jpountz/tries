package net.jpountz.charsequence.collect;

import java.util.List;

import net.jpountz.charsequence.CharComparator;

/**
 * Adapdation of {@link AbstractBinarySearchTrie} to handle char[] keys.
 */
class CharArrayBinarySearchTrie<T> extends AbstractBinarySearchTrie<char[], T> {

	/**
	 * @param keys the keys, sorted lexicographically
	 * @param values the values
	 */
	public CharArrayBinarySearchTrie(List<char[]> keys, List<T> values, CharComparator comparator) {
		super(keys, values, comparator);
	}

	@Override
	protected int compare(
			char[] key1, int off1, int len1,
			char[] key2, int off2, int len2) {
		return comparator.compare(key1, off1, len1, key2, off2, len2);
	}

	@Override
	protected int compare(
			CharSequence key1, int off1, int len1,
			char[] key2, int off2, int len2) {
		return comparator.compare(key1, off1, len1, key2, off2, len2);
	}

	@Override
	protected int size(char[] key) {
		return key.length;
	}

	@Override
	protected char charAt(char[] key, int offset) {
		return key[offset];
	}

}
