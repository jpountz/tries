package net.jpountz.trie;

public class SimpleTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new SimpleTrie<Integer>();
	}

}
