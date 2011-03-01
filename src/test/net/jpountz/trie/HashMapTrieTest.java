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
	public void testCursor2() {
		// not supported
	}

	@Override
	public void testMoveToNextSuffix() {
		// not supported
	}

	@Override
	public void testGetNeightbors() {
		// not supported
	}

	@Override
	public void testEmptyTrie() {
		// not supported
	}

	@Override
	public void testMoveToNextNodeDF() {
		// not supported
	}

	@Override
	public void testMoveToNextNodeBF() {
		// not supported
	}
	
	@Override
	public void testMoveToNextNodeBFTD() {
		// not supported
	}
}
