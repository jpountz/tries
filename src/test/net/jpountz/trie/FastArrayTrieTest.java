package net.jpountz.trie;

public class FastArrayTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new FastArrayTrie<Integer>();
	}

}
