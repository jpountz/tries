package net.jpountz.trie;

public class ArrayTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new ArrayTrie<Integer>(10,2);
	}

}
