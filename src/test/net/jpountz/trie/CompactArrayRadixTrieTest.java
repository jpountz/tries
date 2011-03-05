package net.jpountz.trie;

public class CompactArrayRadixTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new CompactArrayRadixTrie<Integer>();
	}

}
