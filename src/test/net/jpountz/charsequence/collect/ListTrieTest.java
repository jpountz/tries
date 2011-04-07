package net.jpountz.charsequence.collect;

import net.jpountz.charsequence.collect.ListTrie;
import net.jpountz.charsequence.collect.Trie;

public class ListTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newMap() {
		return new ListTrie<Integer>();
	}

}
