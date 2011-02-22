package net.jpountz.trie;

public class Char2ObjectRBTreeMapTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new Char2ObjectRBTreeMapTrie<Integer>();
	}

}
