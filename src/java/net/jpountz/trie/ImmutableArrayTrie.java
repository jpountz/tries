package net.jpountz.trie;

abstract class ImmutableArrayTrie<T> extends ArrayTrie<T> {

	public ImmutableArrayTrie(int initialCapacity, float growthFactor,
			int size, Object[] values, int[] brothers, char[] labels) {
		super(initialCapacity, growthFactor, size, values, brothers, labels, 0, EMPTY_INT_ARRAY);
	}

	@Override
	protected boolean removeChild(int node, char c) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void removeChildren(int node) {
		throw new UnsupportedOperationException();	
	}

	@Override
	protected int addChild(int node, char c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void ensureCapacity(int capacity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

}
