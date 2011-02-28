package net.jpountz.trie;

public class CompactArrayTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new CompactArrayTrie<Integer>();
	}

}
