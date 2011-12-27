package net.jpountz.charsequence.collect;

import java.util.Arrays;

import net.jpountz.charsequence.GrowthStrategy;

/**
 * An array trie. This trie provides O(1) access from a node to any of its
 * children.
 */
public class ArrayTrie<T> extends AbstractListTrie<T> implements Trie.Optimizable, Trie.Trimmable {

	protected static final int DEFAULT_CHILDREN_CAPACITY = 5;
	protected static final float DEFAULT_CHILDREN_GROWTH_FACTOR = 2f;
	protected static final int[] NO_CHILD = new int[] { 0 };

	protected final int initialChildrenCapacity;
	protected final GrowthStrategy childrenGrowthStrategy;
	protected int[][] children;

	public ArrayTrie(int initialCapacity, GrowthStrategy growthStrategy,
			int initialChildrenCapacity, GrowthStrategy childrenGrowthStrategy) {
		super(initialCapacity, growthStrategy);
		this.initialChildrenCapacity = initialChildrenCapacity;
		this.childrenGrowthStrategy = childrenGrowthStrategy;
		children = new int[initialCapacity][];
		Arrays.fill(children, NO_CHILD);
	}

	public ArrayTrie() {
		this(DEFAULT_CAPACITY, GrowthStrategy.FAST_GROWTH,
				DEFAULT_CHILDREN_CAPACITY, GrowthStrategy.FAST_GROWTH);
	}

	@Override
	protected void setDeleted(int node) {
		super.setDeleted(node);
		children[node] = NO_CHILD;
	}

	@Override
	protected int firstChild(int position) {
		int[] cs = children[position];
		if (cs[0] > 0) {
			return cs[1];
		}
		return NOT_FOUND;
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
						newCapacity = growthStrategy.grow(newCapacity);
					}
					int[] newCs = new int[newCapacity+1];
					Arrays.fill(newCs, NOT_FOUND);
					System.arraycopy(cs, 1, newCs, 1-offset, size);
					cs = newCs;
				} else {
					System.arraycopy(cs, 1, cs, 1-offset, size);
				}
				Arrays.fill(cs, 1, 1-offset, NOT_FOUND);
				child = newNode();
				setLabel(child, c);
				size = cs[0] = newSize;
				offset = 0;
				setBrother(NOT_FOUND, child, firstChild);
			} else if (offset >= size) {
				int newSize = offset+1;
				if (1+newSize > cs.length) {
					int previousCapacity = cs.length - 1;
					int newCapacity = previousCapacity;
					while (newCapacity < newSize) {
						newCapacity = growthStrategy.grow(newCapacity);
					}
					cs = Arrays.copyOf(cs, newCapacity + 1);
					Arrays.fill(cs, previousCapacity + 1, newCapacity + 1, NOT_FOUND);
				}
				child = newNode();
				setLabel(child, c);
				setBrother(NOT_FOUND, cs[size], child);
				size = cs[0] = newSize;
			} else {
				child = cs[1+offset];
				if (child == NOT_FOUND) {
					child = newNode();
					setLabel(child, c);
					for (int i = offset+1; i < size; ++i) {
						int next = cs[i+1];
						if (next != NOT_FOUND) {
							setBrother(NOT_FOUND, child, next);
							break;
						}
					}
					for (int i = offset-1; i >= 0; --i) {
						int previous = cs[1+i];
						if (previous != NOT_FOUND) {
							setBrother(NOT_FOUND, previous, child);
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
						setBrother(NOT_FOUND, cs[1+i], NOT_FOUND);
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
							setBrother(NOT_FOUND, cs[i+1], brother(child));
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
			}
			children[node] = NO_CHILD;
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
		ArrayTrie<T> trie = new ArrayTrie<T>(size(), growthStrategy,
				initialChildrenCapacity, childrenGrowthStrategy);
		AbstractListTrieCursor<T> cursor = getCursor();
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

}
