package net.jpountz.charsequence.collect;

import java.util.ArrayList;
import java.util.List;

public class BinarySearchTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newMap() {
		return Tries.sortedCharSequenceListAsTrie(new ArrayList<String>(), new ArrayList<Integer>());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void put(String key, Integer value) {
		List<String> keys = ((AbstractBinarySearchTrie<String, Integer>) trie).keys;
		List<Integer> values = ((AbstractBinarySearchTrie<String, Integer>) trie).values;

		int i = ((AbstractBinarySearchTrie<char[], Integer>) trie).binarySearch(key, 0, key.length());
		if (i >= 0) {
			values.set(i, value);
		} else {
			i = -1 - i;
			keys.add(i, key);
			values.add(i, value);
		}
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
