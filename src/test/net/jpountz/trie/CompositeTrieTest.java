package net.jpountz.trie;

public class CompositeTrieTest extends AbstractTrieTest {

	@Override
	public Trie<Integer> newTrie() {
		return new CompositeTrie<Integer>(new TrieFactory() {
			@Override
			public <V> Trie<V> newTrie() {
				return new FastCharMapTrie<V>();
			}
		},
		new TrieFactory() {
			@Override
			public <V> Trie<V> newTrie() {
				return new ArrayTrie<V>();
			}
		}, 2);
	}

}
