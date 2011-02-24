package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

/**
 * Trie implementation based on 3 backing arrays.
 */
public class ArrayTrie<T> extends AbstractTrie<T> {

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

	private static final class ArrayTrieCursor<T> extends AbstractCursor<T> {

		final ArrayTrie<T> trie;
		int current;
		IntArrayList parents;

		protected ArrayTrieCursor(ArrayTrie<T> trie, int current,
				IntArrayList parents, StringBuilder label) {
			super(label);
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		public ArrayTrieCursor(ArrayTrie<T> trie) {
			this(trie, START, new IntArrayList(), new StringBuilder());
		}

		@Override
		public Node getNode() {
			return new ArrayTrieNode(current);
		}

		@Override
		public boolean moveToChild(char c) {
			int child = trie.getChildNode(current, c);
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
			int child = trie.children[current];
			if (child == NOT_FOUND) {
				return false;
			} else {
				parents.push(current);
				current = child;
				label.append(trie.labels[current]);
				return true;
			}
		}

		@Override
		public boolean moveToBrother() {
			int brother = trie.brothers[current];
			if (brother == NOT_FOUND) {
				return false;
			} else {
				current = brother;
				label.setCharAt(label.length() - 1, trie.labels[current]);
				return true;
			}
		}

		@Override
		public void addChild(char c) {
			parents.push(current);
			label.append(c);
			current = trie.addChildNode(current, c);
		}

		@Override
		public boolean removeChild(char c) {
			// TODO
			return false;
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			int node = trie.children[current];
			while (node != NOT_FOUND) {
				children.add(trie.labels[node]);
				node = trie.brothers[node];
			}
		}

		@Override
		public int getChildrenSize() {
			int size = 0;
			int node = trie.children[current];
			while (node != NOT_FOUND) {
				++size;
				node = trie.brothers[node];
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
				int node = trie.children[current];
				while (node != NOT_FOUND) {
					result += sizeUnder(node);
					node = trie.brothers[node];
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

	private static final int START = 0;
	private static final int NOT_FOUND = -1;

	private static final int DEFAULT_CAPACITY = 255;
	private static final float DEFAULT_GROWTH_FACTOR = 2f;

	private final float growthFactor;
	private int capacity;
	private int size;

	private int[] children;
	private int[] brothers;
	private char[] labels;
	private Object[] values;

	public ArrayTrie(int initialCapacity, float growthFactor) {
		validate(initialCapacity, growthFactor);
		this.capacity = initialCapacity;
		this.growthFactor = growthFactor;
		size = 1;
		children = new int[capacity];
		Arrays.fill(children, NOT_FOUND);
		brothers = new int[capacity];
		Arrays.fill(brothers, NOT_FOUND);
		labels = new char[capacity];
		values = new Object[capacity];
	}

	public ArrayTrie() {
		this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR);
	}

	public void ensureCapacity(int capacity) {
		if (capacity > this.capacity) {
			children = Arrays.copyOf(children, capacity);
			Arrays.fill(children, this.capacity, capacity, NOT_FOUND);
			brothers = Arrays.copyOf(brothers, capacity);
			Arrays.fill(brothers, this.capacity, capacity, NOT_FOUND);
			labels = Arrays.copyOf(labels, capacity);
			values = Arrays.copyOf(values, capacity);
			this.capacity = capacity;
		}
	}

	private int newNode() {
		if (size >= capacity) {
			ensureCapacity((int) Math.ceil(growthFactor * capacity));
		}
		return size++;
	}

	@Override
	public Cursor<T> getCursor() {
		return new ArrayTrieCursor<T>(this);
	}

	@Override
	public void trimToSize() {
		children = Arrays.copyOf(children, size);
		brothers = Arrays.copyOf(brothers, size);
		labels = Arrays.copyOf(labels, size);
		values = Arrays.copyOf(values, size);
		capacity = size;
	}

	public int getChildNode(int node, char label) {
		int child = children[node];
		while (child != NOT_FOUND) {
			if (labels[child] == label) {
				return child;
			} else if (labels[child] > label) {
				break;
			} else {
				child = brothers[child];
			}
		}
		return NOT_FOUND;
	}

	private int addChildNode(int node, char c) {
		int firstChild = children[node];
		if (firstChild == NOT_FOUND) {
			firstChild = newNode();
			labels[firstChild] = c;
			children[node] = firstChild;
			node = firstChild;
		} else {
			char firstChildLabel = labels[firstChild];
			if (firstChildLabel == c) {
				node = firstChild;
			} else if (firstChildLabel > c) {
				int newFirstChild = newNode();
				labels[newFirstChild] = c;
				children[node] = newFirstChild;
				brothers[newFirstChild] = firstChild;
				node = newFirstChild;
			} else {
				int brother = firstChild;
				while (true) {
					int followingBrother = brothers[brother];
					if (followingBrother == NOT_FOUND) {
						followingBrother = newNode();
						labels[followingBrother] = c;
						brothers[brother] = followingBrother;
						node = followingBrother;
						break;
					}
					char followingBrotherLabel = labels[followingBrother];
					if (followingBrotherLabel == c) {
						node = followingBrother;
						break;
					} else if (followingBrotherLabel > c) {
						int newBrother = newNode();
						labels[newBrother] = c;
						brothers[brother] = newBrother;
						brothers[newBrother] = followingBrother;
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
	public void put(char[] buffer, int offset, int length, T value) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = buffer[offset+i];
			node = addChildNode(node, c);
		}
		values[node] = value;
	}

	@Override
	public void put(CharSequence sequence, int offset, int length, T value) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = sequence.charAt(offset+i);
			node = addChildNode(node, c);
		}
		values[node] = value;
	}

	public int getNode(char[] buffer, int offset, int length) {
		int node = START;
		for (int i = 0; i < length; ++i) {
			char c = buffer[offset+i];
			node = getChildNode(node, c);
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
			node = getChildNode(node, c);
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

	@Override
	public void clear() {
		Arrays.fill(children, -1);
		Arrays.fill(brothers, -1);
		Arrays.fill(values, null);
	}

}
