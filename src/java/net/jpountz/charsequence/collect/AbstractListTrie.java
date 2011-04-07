package net.jpountz.charsequence.collect;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

import net.jpountz.charsequence.CharComparator;
import net.jpountz.charsequence.GrowthStrategy;

/**
 * Trie implementation where each node maintains a linked list of its children
 * for fast traversal.
 */
abstract class AbstractListTrie<T> extends AbstractTrie<T> {

	protected static final int[] EMPTY_INT_ARRAY = new int[0];
	protected final CharComparator comparator = CharComparator.DEFAULT;

	private static class ArrayTrieNode implements Node {

		final int position;

		public ArrayTrieNode(int position) {
			this.position = position;
		}

		@Override
		public int hashCode() {
			return position;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArrayTrieNode other = (ArrayTrieNode) obj;
			if (position != other.position)
				return false;
			return true;
		}

	}

	static final class ArrayTrieCursor<T> extends AbstractCursor<T> {

		private StringBuilder label;
		final AbstractListTrie<T> trie;
		int current;
		final IntArrayList parents;

		protected ArrayTrieCursor(AbstractListTrie<T> trie, int current,
				IntArrayList parents, StringBuilder label) {
			this.label = label;
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		public ArrayTrieCursor(AbstractListTrie<T> trie) {
			this(trie, START, new IntArrayList(), new StringBuilder());
		}

		@Override
		protected CharSequence getLabelInternal() {
			return label;
		}

		public int getNodeId() {
			return current;
		}

		@Override
		public Node getNode() {
			return new ArrayTrieNode(current);
		}

		@Override
		public boolean moveToChild(char c) {
			int child = trie.child(current, c);
			if (child == NOT_FOUND) {
				return false;
			} else {
				parents.push(current);
				current = child;
				label.append(c);
				return true;
			}
		}

		@Override
		public boolean moveToFirstChild() {
			int child = trie.firstChild(current);
			if (child == NOT_FOUND) {
				return false;
			} else {
				parents.push(current);
				current = child;
				label.append(trie.label(current));
				return true;
			}
		}

		@Override
		public boolean moveToBrother() {
			int brother = trie.brother(current);
			if (brother == NOT_FOUND) {
				return false;
			} else {
				current = brother;
				label.setCharAt(label.length() - 1, trie.label(current));
				return true;
			}
		}

		@Override
		public void addChild(char c) {
			parents.push(current);
			label.append(c);
			current = trie.addChild(current, c);
			if (current == 0) {
				System.out.println("error");
			}
		}

		@Override
		public boolean removeChild(char c) {
			return trie.removeChild(current, c);
		}

		@Override
		public void removeChildren() {
			trie.removeChildren(current);
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			int node = trie.firstChild(current);
			while (node != NOT_FOUND) {
				children.add(trie.label(node));
				node = trie.brother(node);
			}
		}

		@Override
		public int getChildrenSize() {
			int size = 0;
			int node = trie.firstChild(current);
			while (node != NOT_FOUND) {
				++size;
				node = trie.brother(node);
			}
			return size;
		}

		@Override
		public boolean moveToParent() {
			if (parents.isEmpty()) {
				return false;
			} else {
				current = parents.popInt();
				label.setLength(label.length() - 1);
				return true;
			}
		}

		@Override
		public boolean isAtRoot() {
			return current == START;
		}

		@Override
		public boolean isAt(Node under) {
			return current == ((ArrayTrieNode) under).position;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getValue() {
			return (T) trie.values[current];
		}

		@Override
		public void setValue(T value) {
			trie.values[current] = value;
		}

		private int sizeUnder(int position) {
			if (position == NOT_FOUND) {
				return 0;
			} else {
				int result = 1;
				int node = trie.firstChild(current);
				while (node != NOT_FOUND) {
					result += sizeUnder(node);
					node = trie.brother(node);
				}
				return result;
			}
		}

		@Override
		public void reset() {
			current = START;
			parents.clear();
			label.setLength(0);
		}

		@Override
		public ArrayTrieCursor<T> clone() {
			return new ArrayTrieCursor<T>(trie, current,
					new IntArrayList(parents),
					new StringBuilder(label));
		}
	}

	protected static final int START = 0;
	protected static final int NOT_FOUND = -1;

	protected static final int DEFAULT_CAPACITY = 1024;
	protected static final float DEFAULT_GROWTH_FACTOR = 2f;

	protected final int initialCapacity;
	protected final GrowthStrategy growthStrategy;
	protected int size;
	protected int deletedCount;
	protected int[] deleted;
	protected Object[] values;
	protected int[] brothers;
	protected char[] labels;

	public AbstractListTrie(int initialCapacity, GrowthStrategy growthStrategy) {
		this(initialCapacity, growthStrategy, 1, new Object[initialCapacity],
				new int[initialCapacity], new char[initialCapacity],
				0, EMPTY_INT_ARRAY);
		Arrays.fill(brothers, NOT_FOUND);
	}

	protected AbstractListTrie(int initialCapacity, GrowthStrategy growthStrategy, int size,
			Object[] values, int[] brothers, char[] labels, int deletedCount,
			int[] deleted) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException("initialCapacity must be > 0");
		}
		this.initialCapacity = initialCapacity;
		if (growthStrategy == null) {
			growthStrategy = GrowthStrategy.FAST_GROWTH;
		}
		this.growthStrategy = growthStrategy;
		this.size = size;
		this.values = values;
		this.brothers = brothers;
		this.labels = labels;
		this.deleted = deleted;
		this.deletedCount = deletedCount;
	}

	protected void setDeleted(int node) {
		if (deletedCount == deleted.length) {
			int newCapacity = Math.max(initialCapacity,
					growthStrategy.grow(deletedCount));
			deleted = Arrays.copyOf(deleted, newCapacity);
		}
		deleted[deletedCount++] = node;
		setBrother(NOT_FOUND, node, NOT_FOUND);
	}

	protected abstract boolean removeChild(int node, char c);
	protected abstract void removeChildren(int node);

	protected int getCapacity() {
		return values.length;
	}

	protected abstract int firstChild(int position);

	protected int child(int node, char label) {
		int child = firstChild(node);
		while (child != NOT_FOUND) {
			int comparison = comparator.compare(label(child), label);
			if (comparison == 0) {
				return child;
			} else if (comparison > 0) {
				break;
			} else {
				child = brother(child);
			}
		}
		return NOT_FOUND;
	}

	protected abstract int addChild(int node, char c);

	protected int brother(int position) {
		return brothers[position];
	}

	protected void setBrother(int firstChild, int position, int brother) {
		brothers[position] = brother;
	}

	protected char label(int position) {
		return labels[position];
	}

	protected void setLabel(int position, char label) {
		labels[position] = label;
	}

	public void ensureCapacity(int capacity) {
		int previousCapacity = getCapacity();
		if (capacity > previousCapacity) {
			brothers = Arrays.copyOf(brothers, capacity);
			Arrays.fill(brothers, previousCapacity, capacity, NOT_FOUND);
			labels = Arrays.copyOf(labels, capacity);
			values = Arrays.copyOf(values, capacity);
		}
	}

	public void trimToSize() {
		brothers = Arrays.copyOf(brothers, size);
		labels = Arrays.copyOf(labels, size);
		values = Arrays.copyOf(values, size);
	}

	@Override
	public void clear() {
		Arrays.fill(brothers, NOT_FOUND);
		Arrays.fill(values, null);
	}

	protected int newNode() {
		if (deletedCount > 0) {
			return deleted[--deletedCount];
		}
		int capacity = getCapacity();
		if (size >= capacity) {
			int newCapacity = Math.max(initialCapacity,
					growthStrategy.grow(capacity));
			ensureCapacity(newCapacity);
		}
		return size++;
	}

	@Override
	public ArrayTrieCursor<T> getCursor() {
		return new ArrayTrieCursor<T>(this);
	}

	@Override
	public T put(char[] buffer, int offset, int length, T value) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = buffer[offset+i];
			node = addChild(node, c);
		}
		@SuppressWarnings("unchecked")
		T result = (T) values[node];
		values[node] = value;
		return result;
	}

	@Override
	public T put(CharSequence sequence, int offset, int length, T value) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = sequence.charAt(offset+i);
			node = addChild(node, c);
		}
		@SuppressWarnings("unchecked")
		T result = (T) values[node];
		values[node] = value;
		return result;
	}

	public int getNode(char[] buffer, int offset, int length) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = buffer[offset+i];
			node = child(node, c);
			if (node == NOT_FOUND) {
				break;
			}
		}
		return node;
	}

	public int getNode(CharSequence sequence, int offset, int length) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = sequence.charAt(offset+i);
			node = child(node, c);
			if (node == NOT_FOUND) {
				break;
			}
		}
		return node;
	}

	@SuppressWarnings("unchecked")
	public T getValue(int node) {
		if (node == NOT_FOUND) {
			return null;
		} else {
			return (T) values[node];
		}
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		return getValue(getNode(buffer, offset, length));
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		return getValue(getNode(sequence, offset, length));
	}

	@Override
	public T remove(char[] buffer, int offset, int length) {
		if (length == 0) {
			T result = get("");
			put("", null);
			return result;
		}
		int node = START, nodeToRemoveParent = START;
		char childToRemove = buffer[offset];
		for (int i = 0; i < length; ++i) {
			char c = buffer[offset+i];
			int child = child(node, c);
			if (child == NOT_FOUND) {
				return null;
			}
			if (brother(firstChild(node)) != NOT_FOUND) {
				// several brothers
				nodeToRemoveParent = node;
				childToRemove = c;
			}
			node = child;
		}
		if (nodeToRemoveParent != NOT_FOUND && firstChild(node) == NOT_FOUND) {
			T result = getValue(child(nodeToRemoveParent, childToRemove));
			removeChild(nodeToRemoveParent, childToRemove);
			return result;
		} else {
			return null;
		}
	}

	@Override
	public T remove(CharSequence sequence, int offset, int length) {
		if (length == 0) {
			T result = getValue(START);
			values[START] = null;
			return result;
		}
		int node = START, nodeToRemoveParent = START;
		char childToRemove = sequence.charAt(offset);
		for (int i = 0; i < length; ++i) {
			char c = sequence.charAt(offset+i);
			int child = child(node, c);
			if (child == NOT_FOUND) {
				return null;
			}
			if (brother(firstChild(node)) != NOT_FOUND) {
				// several brothers
				nodeToRemoveParent = node;
				childToRemove = c;
			}
			node = child;
		}
		if (nodeToRemoveParent != NOT_FOUND && firstChild(node) == NOT_FOUND) {
			T result = getValue(child(nodeToRemoveParent, childToRemove));
			removeChild(nodeToRemoveParent, childToRemove);
			return result;
		} else {
			return null;
		}
	}

	@Override
	public int size() {
		return size - deletedCount;
	}

}
