package net.jpountz.trie;

public class CompositeTrieTest extends AbstractTrieTest {

	private static final TrieFactory<Object> rootFactory = new TrieFactory<Object>() {
		@Override
		public Trie<Object> newTrie() {
			return new FastArrayTrie<Object>();
		}
	};

	private static final TrieFactory<Integer> childFactory = new TrieFactory<Integer>() {
		@Override
		public Trie<Integer> newTrie() {
			return new CompactArrayTrie<Integer>();
		}
	};

	@Override
	public Trie<Integer> newTrie() {
		return new CompositeTrie<Integer>(rootFactory, childFactory, 2);
	}

}
