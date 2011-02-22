package net.jpountz.trie;

public class FastCharMapTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new FastCharMapTrie<Integer>();
	}

}
