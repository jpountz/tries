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
			CharSequence key2, int off2, int len2) {
		return comparator.compare(key1, off1, len1, key2, off2, len2);
	}

	@Override
	protected int compare(
			CharSequence key1, int off1, int len1,
			CharSequence key2, int off2, int len2) {
		return comparator.compare(key1, off1, len1, key2, off2, len2);
	}

	// TODO: Performance is terrible (2x slower) when not copying this method here, why?
	@Override
	int binarySearch(char[] key, int offset, int length) {
		int lo = 0, hi = keys.size()-1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			K midVal = keys.get(mid);
			int cmp = compare(key, offset, length, midVal, 0, size(midVal));
			if (cmp < 0) {
				hi = mid - 1;
			} else if (cmp > 0) {
				lo = mid + 1;
			} else {
				return mid;
			}
		}
		return -(lo + 1);
	}

	// TODO: Performance is terrible (2x slower) when not copying this method here, why?
	@Override
	int binarySearch(CharSequence key, int offset, int length) {
		int lo = 0, hi = keys.size()-1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			K midVal = keys.get(mid);
			int cmp = compare(key, offset, length, midVal, 0, size(midVal));
			if (cmp < 0) {
				hi = mid - 1;
			} else if (cmp > 0) {
				lo = mid + 1;
			} else {
				return mid;
			}
		}
		return -(lo + 1);
	}

	@Override
	protected int size(CharSequence key) {
		return key.length();
	}

	@Override
	protected char charAt(CharSequence key, int offset) {
		return key.charAt(offset);
	}

}
