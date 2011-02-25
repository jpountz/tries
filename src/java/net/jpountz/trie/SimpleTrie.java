package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Simple implementation where each node stores its children sorted
 * lexicographically.
 */
public class SimpleTrie<T> extends AbstractNodeTrie<T> {

	private static final int DEFAULT_CAPACITY = 5;
	private static final float DEFAULT_GROWTH_FACTOR = 2f;

	private static final char[] EMPTY_CHAR_ARRAY = new char[0];
	@SuppressWarnings("rawtypes")
	private static final SimpleTrieNode[] EMPTY_NODE_ARRAY = new SimpleTrieNode[0];

	private static class SimpleTrieNode<T> extends AbstractNodeTrieNode<T> {

		final SimpleTrie<T> trie;
		SimpleTrieNode<T>[] children;
		char[] labels;
		int size = 0;

		@SuppressWarnings("unchecked")
		public SimpleTrieNode(char label, SimpleTrie<T> trie) {
			super(label);
			this.trie = trie;
			labels = EMPTY_CHAR_ARRAY;
			children = EMPTY_NODE_ARRAY;
		}

		@Override
		protected SimpleTrieNode<T> getFirstChild() {
			if (size > 0) {
				return children[0];
			} else {
				return null;
			}
		}

		@Override
		protected SimpleTrieNode<T> getChild(char c) {
			int idx = Arrays.binarySearch(labels, 0, size, c);
			if (idx < 0) {
				return null;
			} else {
				return children[idx];
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected SimpleTrieNode<T> addChild(char c) {
			if (labels.length == 0) {
				children = new SimpleTrieNode[trie.initialCapacity];
				labels = new char[trie.initialCapacity];
				labels[0] = c;
				SimpleTrieNode<T> node = new SimpleTrieNode<T>(c, trie);
				children[0] = node;
				size = 1;
				return node;
			} else {
				int idx = Arrays.binarySearch(labels, 0, size, c);
				if (idx >= 0) {
					return children[idx];
				} else {
					if (size == labels.length) {
						// resize
						int newCapacity = (int) Math.ceil(size * trie.growthFactor);
						children = Arrays.copyOf(children, newCapacity);
						labels = Arrays.copyOf(labels, newCapacity);
					}
					idx = -idx - 1; // insertion point
					for (int i = size; i > idx; --i) {
						labels[i] = labels[i-1];
						children[i] = children[i-1];
					}
					++size;
					labels[idx] = c;
					SimpleTrieNode<T> node = new SimpleTrieNode<T>(c, trie);
					if (idx > 0) {
						children[idx - 1].brother = node;
					}
					if (idx < size - 1) {
						node.brother = children[idx+1];
					}
					children[idx] = node;
					return node;
				}
			}
		}

		@Override
		public boolean removeChild(char c) {
			int idx = Arrays.binarySearch(labels, 0, size, c);
			if (idx >= 0) {
				if (idx > 0) {
					children[idx - 1].brother = children[idx].brother;
				}
				--size;
				for (int i = idx; i < size; ++i) {
					labels[i] = labels[i+1];
					children[i] = children[i+1];
				}
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			for (int i = 0; i < size; ++i) {
				children.add(labels[i]);
			}
		}

		@Override
		protected int childrenSize() {
			return size;
		}

		public int size() {
			int result = 1;
			for (int i = 0; i < size; ++i) {
				result += children[i].size();
			}
			return result;
		}

		public void trimToSize() {
			if (size < labels.length) {
				labels = Arrays.copyOf(labels, size);
				children = Arrays.copyOf(children, size);
				for (int i = 0; i < size; ++i) {
					children[i].trimToSize();
				}
			}
		}

	}

	private static final class SimpleTrieCursor<T> extends AbstractNodeTrieCursor<T> {

		private SimpleTrieCursor(SimpleTrie<T> trie, SimpleTrieNode<T> current,
				Deque<AbstractNodeTrieNode<T>> parents, StringBuilder label) {
			super(trie, current, parents, label);
		}

		public SimpleTrieCursor(SimpleTrie<T> trie) {
			this(trie, trie.root, new ArrayDeque<AbstractNodeTrieNode<T>>(),
					new StringBuilder());
		}

		public SimpleTrieCursor<T> clone() {
			return new SimpleTrieCursor<T>((SimpleTrie<T>) trie,
					(SimpleTrieNode<T>) current,
					new ArrayDeque<AbstractNodeTrieNode<T>>(parents),
					new StringBuilder(label));
		}

	}

	private int initialCapacity;
	private float growthFactor;
	private final SimpleTrieNode<T> root;

	/**
	 * @param initialCapacity initial number of children for a single node
	 * @param growthFactor growth factor for the number of children
	 */
	public SimpleTrie(int initialCapacity, float growthFactor) {
		root = new SimpleTrieNode<T>('\0', this);
		validate(initialCapacity, growthFactor);
		this.initialCapacity = initialCapacity;
		this.growthFactor = growthFactor;
	}

	public SimpleTrie() {
		this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR);
	}

	@Override
	protected AbstractNodeTrieNode<T> getRoot() {
		return root;
	}

	public void clear() {
		root.children = null;
		root.labels = null;
		root.value = null;
	}

	@Override
	public Cursor<T> getCursor() {
		return new SimpleTrieCursor<T>(this);
	}

	@Override
	public void trimToSize() {
		root.trimToSize();
	}
}