package net.jpountz.trie;

import java.util.Arrays;

public class CompactArrayTrie<T> extends ArrayTrie<T> implements Trie.Optimizable, Trie.Trimmable {

	private int[] children;

	public CompactArrayTrie(int initialCapacity, float growthFactor) {
		super(initialCapacity, growthFactor);
		children = new int[initialCapacity];
		Arrays.fill(children, NOT_FOUND);
	}

	public CompactArrayTrie() {
		this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR);
	}

	@Override
	protected void setDeleted(int node) {
		super.setDeleted(node);
		children[node] = NOT_FOUND;
	}

	@Override
	protected boolean removeChild(int node, char c) {
		int child = child(node);
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
					setBrother(previousChild, brother(child));
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
		int child = child(node);
		while (child != NOT_FOUND) {
			removeChildren(child);
			setDeleted(child);
			int newChild = brother(child);
			setBrother(child, NOT_FOUND);
			child = newChild;
		}
		children[node] = NOT_FOUND;
	}

	protected int child(int position) {
		return children[position];
	}

	protected int addChild(int node, char c) {
		int firstChild = child(node);
		if (firstChild == NOT_FOUND) {
			firstChild = newNode();
			setLabel(firstChild, c);
			children[node] = firstChild;
			node = firstChild;
		} else {
			char firstChildLabel = label(firstChild);
			if (firstChildLabel == c) {
				node = firstChild;
			} else if (firstChildLabel > c) {
				int newFirstChild = newNode();
				setLabel(newFirstChild, c);
				children[node] = newFirstChild;
				setBrother(newFirstChild, firstChild);
				node = newFirstChild;
			} else {
				int brother = firstChild;
				while (true) {
					int followingBrother = brother(brother);
					if (followingBrother == NOT_FOUND) {
						followingBrother = newNode();
						setLabel(followingBrother, c);
						setBrother(brother, followingBrother);
						node = followingBrother;
						break;
					}
					char followingBrotherLabel = label(followingBrother);
					if (followingBrotherLabel == c) {
						node = followingBrother;
						break;
					} else if (followingBrotherLabel > c) {
						int newBrother = newNode();
						setLabel(newBrother, c);
						setBrother(brother, newBrother);
						setBrother(newBrother, followingBrother);
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
		CompactArrayTrie<T> trie = new CompactArrayTrie<T>(size(), growthFactor);
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
