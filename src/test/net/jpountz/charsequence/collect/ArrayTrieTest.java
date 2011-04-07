package net.jpountz.charsequence.collect;

import net.jpountz.charsequence.collect.ArrayTrie;
import net.jpountz.charsequence.collect.Trie;

public class ArrayTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newMap() {
		return new ArrayTrie<Integer>();
	}

}
