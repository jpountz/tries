package net.jpountz.charsequence.collect;


import java.util.Arrays;

import net.jpountz.charsequence.GrowthStrategy;

public class ListTrie<T> extends AbstractListTrie<T> implements Trie.Optimizable, Trie.Trimmable {

	private int[] children;

	public ListTrie(int initialCapacity, GrowthStrategy growthStrategy) {
		super(initialCapacity, growthStrategy);
		children = new int[initialCapacity];
		Arrays.fill(children, NOT_FOUND);
	}

	public ListTrie() {
		this(DEFAULT_CAPACITY, GrowthStrategy.FAST_GROWTH);
	}

	@Override
	protected void setDeleted(int node) {
		super.setDeleted(node);
		children[node] = NOT_FOUND;
	}

	@Override
	protected boolean removeChild(int node, char c) {
		int firstChild = firstChild(node);
		int child = firstChild;
		if (child == NOT_FOUND) {
			return false;
		} else if (label(child) == c) {
			children[node] = brother(child);
			removeChildren(child);
			setDeleted(child);
			return true;
		} else if (label(child) < c) {
			int previousChild = child;
			while (true) {
				previousChild = child;
				child = brother(child);
				if (child == NOT_FOUND) {
					return false;
				}
				if (label(child) == c) {
					removeChildren(child);
					setBrother(firstChild, previousChild, brother(child));
					setDeleted(child);
					return true;
				} else if (label(child) > c) {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	@Override
	protected void removeChildren(int node) {
		int child = firstChild(node);
		while (child != NOT_FOUND) {
			removeChildren(child);
			setDeleted(child);
			int newChild = brother(child);
			setBrother(NOT_FOUND, child, NOT_FOUND);
			child = newChild;
		}
		children[node] = NOT_FOUND;
	}

	protected int firstChild(int position) {
		return children[position];
	}

	protected int addChild(int node, char c) {
		int firstChild = firstChild(node);
		if (firstChild == NOT_FOUND) {
			firstChild = newNode();
			setLabel(firstChild, c);
			children[node] = firstChild;
			node = firstChild;
		} else {
			char firstChildLabel = label(firstChild);
			int comparison = comparator.compare(firstChildLabel, c);
			if (comparison == 0) {
				node = firstChild;
			} else if (comparison > 0) {
				int newFirstChild = newNode();
				setLabel(newFirstChild, c);
				children[node] = newFirstChild;
				setBrother(newFirstChild, newFirstChild, firstChild);
				node = newFirstChild;
			} else {
				int brother = firstChild;
				while (true) {
					int followingBrother = brother(brother);
					if (followingBrother == NOT_FOUND) {
						followingBrother = newNode();
						setLabel(followingBrother, c);
						setBrother(firstChild, brother, followingBrother);
						node = followingBrother;
						break;
					}
					char followingBrotherLabel = label(followingBrother);
					comparison = comparator.compare(followingBrotherLabel, c);
					if (comparison == 0) {
						node = followingBrother;
						break;
					} else if (comparison > 0) {
						int newBrother = newNode();
						setLabel(newBrother, c);
						setBrother(firstChild, brother, newBrother);
						setBrother(firstChild, newBrother, followingBrother);
						node = newBrother;
						break;
					}
					brother = followingBrother;
				}
			}
		}
		return node;
	}

	@Override
	public void ensureCapacity(int capacity) {
		int previousCapacity = getCapacity();
		super.ensureCapacity(capacity);
		if (capacity > previousCapacity) {
			children = Arrays.copyOf(children, capacity);
			Arrays.fill(children, previousCapacity, capacity, NOT_FOUND);
		}
	}

	@Override
	public void trimToSize() {
		super.trimToSize();
		children = Arrays.copyOf(children, size);
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(children, NOT_FOUND);
	}

	public void optimizeFor(Trie.Traversal traversal) {
		ListTrie<T> trie = new ListTrie<T>(size(), growthStrategy);
		ArrayTrieCursor<T> cursor = getCursor();
		Node node = cursor.getNode();
		do {
			trie.put(cursor.getLabelInternal(), cursor.getValue());
		} while (traversal.moveToNextNode(node, cursor));
		this.brothers = trie.brothers;
		this.children = trie.children;
		this.labels = trie.labels;
		this.values = trie.values;
	}
}
