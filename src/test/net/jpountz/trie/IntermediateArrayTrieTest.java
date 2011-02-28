package net.jpountz.trie;

public class IntermediateArrayTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new IntermediateArrayTrie<Integer>();
	}

}
