package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

import net.jpountz.trie.Trie.Optimizable;

/**
 * Trie implementation based on backing arrays.
 */
abstract class ArrayTrie<T> extends AbstractTrie<T> implements Optimizable {

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
		final ArrayTrie<T> trie;
		int current;
		final IntArrayList parents;

		protected ArrayTrieCursor(ArrayTrie<T> trie, int current,
				IntArrayList parents, StringBuilder label) {
			this.label = label;
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		@Override
		protected CharSequence getLabelInternal() {
			return label;
		}

		public ArrayTrieCursor(ArrayTrie<T> trie) {
			this(trie, START, new IntArrayList(), new StringBuilder());
		}

		@Override
		public Node getBrotherNode() {
			int brother = trie.brother(current);
			if (brother == NOT_FOUND) {
				return null;
			} else {
				return new ArrayTrieNode(brother);
			}
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
			if (child == 0) {
				System.out.println("error");
				trie.child(current, c);
			}
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
			int child = trie.child(current);
			if (child == 0) {
				System.out.println("error");
			}
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
			// TODO
			return false;
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			int node = trie.child(current);
			while (node != NOT_FOUND) {
				children.add(trie.label(node));
				node = trie.brother(node);
			}
		}

		@Override
		public int getChildrenSize() {
			int size = 0;
			int node = trie.child(current);
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
				int node = trie.child(current);
				while (node != NOT_FOUND) {
					result += sizeUnder(node);
					node = trie.brother(node);
				}
				return result;
			}
		}

		@Override
		public int size() {
			return sizeUnder(current);
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

	protected static final int DEFAULT_CAPACITY = 255;
	protected static final float DEFAULT_GROWTH_FACTOR = 2f;

	protected final float growthFactor;
	protected int size;
	protected Object[] values;
	protected int[] brothers;
	protected char[] labels;

	public ArrayTrie(int initialCapacity, float growthFactor) {
		this(initialCapacity, growthFactor, 1, new Object[initialCapacity],
				new int[initialCapacity], new char[initialCapacity]);
		validate(initialCapacity, growthFactor);
		Arrays.fill(brothers, NOT_FOUND);
	}

	protected ArrayTrie(int initialCapacity, float growthFactor, int size,
			Object[] values, int[] brothers, char[] labels) {
		validate(initialCapacity, growthFactor);
		this.growthFactor = growthFactor;
		this.size = size;
		this.values = values;
		this.brothers = brothers;
		this.labels = labels;
	}

	protected int getCapacity() {
		return values.length;
	}

	protected abstract int child(int position);

	protected int child(int node, char label) {
		int child = child(node);
		while (child != NOT_FOUND) {
			if (label(child) == label) {
				return child;
			} else if (label(child) > label) {
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

	protected void setBrother(int position, int brother) {
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

	@Override
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
		int capacity = getCapacity();
		if (size >= capacity) {
			ensureCapacity((int) Math.ceil(growthFactor * capacity));
		}
		return size++;
	}

	@Override
	public ArrayTrieCursor<T> getCursor() {
		return new ArrayTrieCursor<T>(this);
	}

	@Override
	public void put(char[] buffer, int offset, int length, T value) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = buffer[offset+i];
			node = addChild(node, c);
		}
		values[node] = value;
	}

	@Override
	public void put(CharSequence sequence, int offset, int length, T value) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = sequence.charAt(offset+i);
			node = addChild(node, c);
		}
		values[node] = value;
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
	public int size() {
		return size;
	}

}
