package net.jpountz.charsequence.collect;

import java.util.Arrays;

import net.jpountz.charsequence.CharSequenceUtils;

public class BinarySearchTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newMap() {
		return new BinarySearchTrie<Integer>(new char[0][], new Integer[0], null);
	}

	@Override
	protected void put(String key, Integer value) {
		char[][] keys = ((BinarySearchTrie<Integer>) trie).keys;
		Integer[] values = ((BinarySearchTrie<Integer>) trie).values;

		int i = ((BinarySearchTrie<Integer>) trie).binarySearch(key, 0, key.length());
		if (i >= 0) {
			values[i] = value;
		} else {
			i = -1 - i;
			keys = Arrays.copyOf(keys, keys.length + 1);
			values = Arrays.copyOf(values, values.length + 1);
			System.arraycopy(keys, i, keys, i+1, keys.length-i-1);
			System.arraycopy(values, i, values, i+1, values.length-i-1);
			keys[i] = CharSequenceUtils.toChars(key, 0, key.length());
			values[i] = value;
		}
		map = trie = new BinarySearchTrie<Integer>(keys, values, null);
	}

	@Override
	public void testRemove() {
		// unsupported
	}

	@Override
	public void testCursorRW() {
		// unsupported
	}

}
