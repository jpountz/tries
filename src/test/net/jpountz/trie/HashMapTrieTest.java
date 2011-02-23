package net.jpountz.trie;

public class HashMapTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new HashMapTrie<Integer>();
	}

	@Override
	public void testCursor() {
		// not supported
	}

	@Override
	public void testMoveToNextSuffix() {
		// not supported
	}
}
