package net.jpountz.trie;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Arrays;

public class FastArrayTrie<T> extends ArrayTrie<T> implements Trie.Optimizable, Trie.Trimmable {

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
	protected void setDeleted(int node) {
		super.setDeleted(node);
		children[node] = NO_CHILD;
	}

	@Override
	protected int child(int position) {
		int[] cs = children[position];
		if (cs[0] == 0) {
			return NOT_FOUND;
		} else {
			return cs[1];
		}
	}

	@Override
	protected int child(int node, char label) {
		int[] cs = children[node];
		int size = cs[0];
		if (size > 0) {
			int firstChild = cs[1];
			char firstChildLabel = label(firstChild);
			int offset = label - firstChildLabel;
			if (offset >= 0 && offset < size) {
				return cs[offset+1];
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
	protected boolean removeChild(int node, char c) {
		int[] cs = children[node];
		if (cs[0] == 0) {
			return false;
		} else {
			int firstChild = cs[1];
			char firstChildLabel = label(firstChild);
			int offset = c - firstChildLabel;
			if (offset < 0 || offset >= cs[0] || cs[offset+1] == NOT_FOUND) {
				return false;
			} else if (offset == 0) {
				int shift;
				for (shift = 1; shift < cs[0]; ++shift) {
					if (cs[1+shift] != NOT_FOUND) {
						break;
					}
				}
				for (int i = shift; i < cs[0]; ++i) {
					cs[i+1-shift] = cs[i+1];
				}
				cs[0] -= shift;
				removeChildren(firstChild);
				setDeleted(firstChild);
				return true;
			} else if (offset == cs[0] - 1) {
				for (int i = offset - 1; i >= 0; --i) {
					if (cs[1+i] != NOT_FOUND) {
						removeChildren(cs[1+i]);
						setBrother(cs[1+i], NOT_FOUND);
						setDeleted(cs[1+i]);
						cs[0] = 1+i;
						break;
					}
				}
				return true;
			} else {
				int child = cs[offset+1];
				if (child != NOT_FOUND) {
					for (int i = offset - 1; i >= 0; --i) {
						if (cs[i+1] != NOT_FOUND) {
							setBrother(cs[i+1], brother(child));
							break;
						}
					}
					removeChildren(child);
					setDeleted(child);
					cs[offset+1] = NOT_FOUND;
					return true;
				} else {
					return false;
				}
			}
		}
	}

	@Override
	protected void removeChildren(int node) {
		int[] cs = children[node];
		if (cs[0] > 0) {
			for (int i = 1; i <= cs[0]; ++i) {
				int child = cs[i];
				if (child != NOT_FOUND) {
					removeChildren(child);
					setDeleted(child);
				}
				cs[i] = NOT_FOUND;
			}
			cs[0] = 0;
		}
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

	public void optimizeFor(Trie.Traversal traversal) {
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

	/**
	 * Return an immutable copy of this trie. The resulting instance will be at
	 * least as fast, and have a lower memory footprint.
	 *
	 * @return
	 */
	public ImmutableArrayTrie<T> immutableCopy() {
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

	private static class ImmutableFastArrayTrie<T> extends ImmutableArrayTrie<T> {

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

	}
}
