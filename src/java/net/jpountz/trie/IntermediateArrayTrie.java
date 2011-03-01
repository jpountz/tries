package net.jpountz.trie;

import java.util.Arrays;

public class IntermediateArrayTrie<T> extends ArrayTrie<T> {

	protected static final int DEFAULT_CHILDREN_CAPACITY = 5;
	protected static final float DEFAULT_CHILDREN_GROWTH_FACTOR = 2f;
	protected static final char[] NO_CHILD_LABELS = new char[0];
	protected static final int[] NO_CHILD_NODES = new int[0];

	protected final int initialChildrenCapacity;
	protected final float childrenGrowthFactor;
	protected char[][] childrenLabels;
	protected int[][] childrenNodes;
	protected int[] childrenSizes;

	public IntermediateArrayTrie(int initialCapacity, float growthFactor,
			int initialChildrenCapacity, float childrenGrowthFactor) {
		super(initialCapacity, growthFactor);
		validate(initialChildrenCapacity, childrenGrowthFactor);
		this.initialChildrenCapacity = initialChildrenCapacity;
		this.childrenGrowthFactor = childrenGrowthFactor;
		childrenLabels = new char[initialCapacity][];
		Arrays.fill(childrenLabels, NO_CHILD_LABELS);
		childrenNodes = new int[initialCapacity][];
		Arrays.fill(childrenNodes, NO_CHILD_NODES);
		childrenSizes = new int[initialCapacity];
	}

	public IntermediateArrayTrie() {
		this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR,
				DEFAULT_CHILDREN_CAPACITY, DEFAULT_CHILDREN_GROWTH_FACTOR);
	}

	@Override
	protected int child(int position) {
		if (childrenSizes[position] > 0) {
			return childrenNodes[position][0];
		} else {
			return NOT_FOUND;
		}
	}

	@Override
	protected int child(int node, char label) {
		int size = childrenSizes[node];
		if (size > 0) {
			char[] cLabels = childrenLabels[node];
			int idx = Arrays.binarySearch(cLabels, 0, size, label);
			if (idx >= 0) {
				return childrenNodes[node][idx];
			}
		}
		return NOT_FOUND;
	}

	@Override
	protected int addChild(final int node, char c) {
		int size = childrenSizes[node];
		char[] cLabels = childrenLabels[node];
		int idx = Arrays.binarySearch(cLabels, 0, size, c);
		if (idx >= 0) {
			return childrenNodes[node][idx];
		} else {
			// insert
			idx = -1-idx;
			int[] cNodes = childrenNodes[node];
			if (cLabels.length == size) {
				if (size == 0) {
					cLabels = new char[initialChildrenCapacity];
					cNodes = new int[initialChildrenCapacity];
				} else {
					int newCapacity = (int) Math.ceil((double) size * growthFactor);
					cLabels = Arrays.copyOf(cLabels, newCapacity);
					cNodes  = Arrays.copyOf(cNodes, newCapacity);
				}
				childrenLabels[node] = cLabels;
				childrenNodes[node] = cNodes; 
			}
			for (int i = size; i > idx; --i) {
				cLabels[i] = cLabels[i-1];
				cNodes[i] = cNodes[i-1];
			}
			cLabels[idx] = c;
			int child = newNode();
			setLabel(child, c);
			cNodes[idx] = child;
			if (idx > 0) {
				setBrother(cNodes[idx-1], child);
			}
			if (idx < size) {
				setBrother(child, cNodes[idx+1]);
			}
			++childrenSizes[node];
			return child;
		}
	}

	@Override
	public void ensureCapacity(int capacity) {
		int previousCapacity = getCapacity();
		super.ensureCapacity(capacity);
		if (capacity > previousCapacity) {
			childrenLabels = Arrays.copyOf(childrenLabels, capacity);
			Arrays.fill(childrenLabels, previousCapacity, capacity, NO_CHILD_LABELS);
			childrenNodes = Arrays.copyOf(childrenNodes, capacity);
			Arrays.fill(childrenNodes, previousCapacity, capacity, NO_CHILD_NODES);
			childrenSizes = Arrays.copyOf(childrenSizes, capacity);
			Arrays.fill(childrenSizes, previousCapacity, capacity, 0);
		}
	}

	@Override
	public void trimToSize() {
		super.trimToSize();
		childrenLabels = Arrays.copyOf(childrenLabels, size);
		childrenNodes = Arrays.copyOf(childrenNodes, size);
		childrenSizes = Arrays.copyOf(childrenSizes, size);
		for (int i = 0; i < size; ++i) {
			int size = childrenSizes[i];
			if (size == 0) {
				childrenLabels[i] = NO_CHILD_LABELS;
				childrenNodes[i] = NO_CHILD_NODES;
			} else {
				childrenLabels[i] = Arrays.copyOf(childrenLabels[i], size);
				childrenNodes[i] = Arrays.copyOf(childrenNodes[i], size);
			}
		}
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(childrenSizes, 0);
		Arrays.fill(childrenLabels, NO_CHILD_LABELS);
		Arrays.fill(childrenNodes, NO_CHILD_NODES);
	}

	public void optimizeFor(Trie.Traversal traversal) {
		IntermediateArrayTrie<T> trie = new IntermediateArrayTrie<T>(size(), growthFactor,
				initialChildrenCapacity, childrenGrowthFactor);
		ArrayTrieCursor<T> cursor = getCursor();
		Node node = cursor.getNode();
		do {
			trie.put(cursor.getLabelInternal(), cursor.getValue());
		} while (traversal.moveToNextNode(node, cursor));
		trie.trimToSize();
		this.brothers = trie.brothers;
		this.childrenSizes = trie.childrenSizes;
		this.childrenLabels = trie.childrenLabels;
		this.childrenNodes = trie.childrenNodes;
		this.labels = trie.labels;
		this.values = trie.values;
	}

	/*public ArrayTrie<T> immutableCopy() {
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
	}*/
}
