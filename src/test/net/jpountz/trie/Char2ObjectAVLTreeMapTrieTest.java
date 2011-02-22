package net.jpountz.trie;

public class Char2ObjectAVLTreeMapTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new Char2ObjectAVLTreeMapTrie<Integer>();
	}

}
