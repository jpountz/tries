package net.jpountz.trie;


class ImmutableFastArrayTrie<T> extends ArrayTrie<T> {

	private final int[] childrenOffsets;
	private final int[] children;

	ImmutableFastArrayTrie(int size, Object[] values,
			int[] brothers, char[] labels, int[] childrenOffsets,
			int[] children) {
		super(1, 2f, size, values, brothers, labels);
		this.childrenOffsets = childrenOffsets;
		this.children = children;
	}

	@Override
	protected int child(int position) {
		int offset = childrenOffsets[position];
		int nextOffset = childrenOffsets[position+1];
		if (nextOffset > offset) {
			return children[offset];
		} else {
			return NOT_FOUND;
		}
	}

	@Override
	protected int child(int position, char label) {
		int offset = childrenOffsets[position];
		int length = childrenOffsets[position+1] - offset;
		if (length > 0) {
			int o = label - label(children[offset]);
			if (o < length) {
				return children[offset + o];
			}
		}
		return NOT_FOUND;
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
	public void trimToSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void optimizeFor(Trie.Traversal traversal) {
		// read-only
	}
}
