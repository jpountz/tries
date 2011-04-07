package net.jpountz.charsequence.collect;

import net.jpountz.charsequence.collect.ListTrie;
import net.jpountz.charsequence.collect.CompositeTrie;
import net.jpountz.charsequence.collect.ArrayTrie;
import net.jpountz.charsequence.collect.Trie;
import net.jpountz.charsequence.collect.TrieFactory;

public class CompositeTrieTest extends AbstractTrieTest {

	private static final TrieFactory<Object> rootFactory = new TrieFactory<Object>() {
		@Override
		public Trie<Object> newTrie() {
			return new ArrayTrie<Object>();
		}
	};

	private static final TrieFactory<Integer> childFactory = new TrieFactory<Integer>() {
		@Override
		public Trie<Integer> newTrie() {
			return new ListTrie<Integer>();
		}
	};

	@Override
	public Trie<Integer> newMap() {
		return new CompositeTrie<Integer>(rootFactory, childFactory, 2);
	}

}
