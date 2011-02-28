package net.jpountz.trie;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Arrays;

public class FastArrayTrie<T> extends ArrayTrie<T> {

	protected static final int DEFAULT_CHILDREN_CAPACITY = 5;
	protected static final float DEFAULT_CHILDREN_GROWTH_FACTOR = 2f;
	protected static final int[] NO_CHILD = new int[] { 0 };

	protected final int initialChildrenCapacity;
	protected final float childrenGrowthFactor;
	protected int[][] children;

	public FastArrayTrie(int initialCapacity, float growthFactor,
			int initialChildrenCapacity, float childrenGrowthFactor) {
		super(initialCapacity, growthFactor);
		validate(initialChildrenCapacity, childrenGrowthFactor);
		this.initialChildrenCapacity = initialChildrenCapacity;
		this.childrenGrowthFactor = childrenGrowthFactor;
		children = new int[initialCapacity][];
		Arrays.fill(children, NO_CHILD);
	}

	public FastArrayTrie() {
		this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR,
				DEFAULT_CHILDREN_CAPACITY, DEFAULT_CHILDREN_GROWTH_FACTOR);
	}

	@Override
	protected int child(int position) {
		int[] cs = children[position];
		if (cs[0] > 0) {
			return cs[1];
		} else {
			return NOT_FOUND;
		}
	}

	@Override
	protected int child(int node, char label) {
		int[] cs = children[node];
		int size = cs[0];
		if (size > 0) {
			char firstChildLabel = label(cs[1]);
			int offset = label - firstChildLabel;
			if (offset >= 0 && offset < size) {
				return cs[offset + 1];
			}
		}
		return NOT_FOUND;
	}

	@Override
	protected int addChild(final int node, char c) {
		int[] cs = children[node];
		int size = cs[0];
		int child;
		if (size == 0) {
			if (cs.length == 1) {
				cs = new int[1+initialChildrenCapacity];
				Arrays.fill(cs, 2, cs.length, NOT_FOUND);
			}
			child = newNode();
			setLabel(child, c);
			cs[0] = 1;
			cs[1] = child;
		} else {
			int firstChild = cs[1];
			char firstChildLabel = label(firstChild);
			int offset = c - firstChildLabel;
			if (offset < 0) {
				int newSize = size - offset;
				if (1+newSize > cs.length) {
					int newCapacity = cs.length - 1;
					while (newCapacity < 1+newSize) {
						newCapacity *= growthFactor;
					}
					cs = Arrays.copyOf(cs, newCapacity + 1);
					Arrays.fill(cs, 1+size, 1+newCapacity, NOT_FOUND);
					/*for (int i = size; i < newCapacity; ++i) {
						cs[1+i] = NOT_FOUND;
					}*/
				}
				for (int i = size-1; i >= 0; --i) {
					cs[1+i-offset] = cs[1+i];
				}
				Arrays.fill(cs, 1, 1-offset, NOT_FOUND);
				/*for (int i = -offset-1; i >=0; --i) {
					cs[1+i] = NOT_FOUND;
				}*/
				child = newNode();
				setLabel(child, c);
				size = cs[0] = newSize;
				offset = 0;
				setBrother(child, firstChild);
			} else if (offset >= size) {
				int newSize = offset+1;
				if (1+newSize > cs.length) {
					int previousCapacity = cs.length - 1;
					int newCapacity = previousCapacity;
					while (newCapacity < newSize) {
						newCapacity *= growthFactor;
					}
					cs = Arrays.copyOf(cs, newCapacity + 1);
					Arrays.fill(cs, previousCapacity + 1, newCapacity + 1, NOT_FOUND);
					/*for (int i = previousCapacity; i < newCapacity; ++i) {
						cs[1+i] = NOT_FOUND;
					}*/
				}
				child = newNode();
				setLabel(child, c);
				setBrother(cs[size], child);
				size = cs[0] = newSize;
			} else {
				child = cs[1+offset];
				if (child == NOT_FOUND) {
					child = newNode();
					setLabel(child, c);
					for (int i = offset+1; i < size; ++i) {
						int next = cs[i+1];
						if (next != NOT_FOUND) {
							setBrother(child, next);
							break;
						}
					}
					for (int i = offset-1; i >= 0; --i) {
						int previous = cs[1+i];
						if (previous != NOT_FOUND) {
							setBrother(previous, child);
							break;
						}
					}
				}
			}
			cs[1+offset] = child;
		}
		children[node] = cs;
		return child;
	}


	@Override
	public void ensureCapacity(int capacity) {
		int previousCapacity = getCapacity();
		super.ensureCapacity(capacity);
		if (capacity > previousCapacity) {
			children = Arrays.copyOf(children, capacity);
			Arrays.fill(children, previousCapacity, capacity, NO_CHILD);
		}
	}

	@Override
	public void trimToSize() {
		super.trimToSize();
		children = Arrays.copyOf(children, size);
		for (int i = 0; i < size; ++i) {
			int[] cs = children[i];
			int size = cs[0];
			if (size == 0) {
				children[i] = NO_CHILD;
			} else if (1+size < cs.length) {
				children[i] = Arrays.copyOf(cs, 1+size);
			}
		}
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(children, NO_CHILD);
	}

	public void optimizeFor(TrieTraversal traversal) {
		FastArrayTrie<T> trie = new FastArrayTrie<T>(size(), growthFactor,
				initialChildrenCapacity, childrenGrowthFactor);
		ArrayTrieCursor<T> cursor = getCursor();
		Node node = cursor.getNode();
		do {
			trie.put(cursor.getLabelInternal(), cursor.getValue());
		} while (traversal.moveToNextNode(node, cursor));
		trie.trimToSize();
		this.brothers = trie.brothers;
		this.children = trie.children;
		this.labels = trie.labels;
		this.values = trie.values;
	}

	public ArrayTrie<T> immutableCopy() {
		IntList offsets = new IntArrayList();
		IntList children = new IntArrayList();
		for (int i = 0; i < size; ++i) {
			offsets.add(children.size());
			int[] cs = this.children[i];
			if (cs[0] > 0) {
				for (int j = 0; j < cs[0]; ++j) {
					children.add(cs[1+j]);
				}
			}
		}
		offsets.add(children.size());
		return new ImmutableFastArrayTrie<T>(size, Arrays.copyOf(values, size),
				Arrays.copyOf(brothers, size), Arrays.copyOf(labels, size),
				offsets.toArray(new int[offsets.size()]),
				children.toArray(new int[children.size()]));
	}
}
