package net.jpountz.charsequence.collect;

import net.jpountz.charsequence.collect.ListRadixTrie;
import net.jpountz.charsequence.collect.Trie;

public class ListRadixTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newMap() {
		return new ListRadixTrie<Integer>();
	}

}
