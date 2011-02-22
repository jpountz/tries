package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Simple implementation where each node stores its children sorted
 * lexicographically.
 */
public class SimpleTrie<T> extends AbstractTrie<T> {

	private static final int DEFAULT_CAPACITY = 5;
	private static final float DEFAULT_GROWTH_FACTOR = 2f;

	private static class SimpleTrieNode<T> {
		SimpleTrieNode<T>[] children;
		char[] labels;
		int size = 0;
		T value;

		public int size() {
			int result = 1;
			for (int i = 0; i < size; ++i) {
				result += children[i].size();
			}
			return result;
		}

		public void trimToSize() {
			if (labels != null && size < labels.length) {
				labels = Arrays.copyOf(labels, size);
				children = Arrays.copyOf(children, size);
				for (int i = 0; i < size; ++i) {
					children[i].trimToSize();
				}
			}
		}
	}

	private static class SimpleTrieCursor<T> implements Cursor<T> {

		private final SimpleTrie<T> trie;
		private SimpleTrieNode<T> current;
		private final Deque<SimpleTrieNode<T>> parents;

		private SimpleTrieCursor(SimpleTrie<T> trie, SimpleTrieNode<T> current,
				Deque<SimpleTrieNode<T>> parents) {
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		public SimpleTrieCursor(SimpleTrie<T> trie) {
			this(trie, trie.root, new ArrayDeque<SimpleTrieNode<T>>());
		}

		@Override
		public boolean moveToChild(char c) {
			int idx = Arrays.binarySearch(current.labels, 0, current.size, c);
			if (idx < 0) {
				return false;
			} else {
				parents.push(current);
				current = current.children[idx];
				return true;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addChild(char c) {
			parents.push(current);
			if (current.children == null || current.children.length == 0) {
				current.children = new SimpleTrieNode[trie.initialCapacity];
				current.labels = new char[trie.initialCapacity];
				current.labels[0] = c;
				SimpleTrieNode<T> node = new SimpleTrieNode<T>();
				current.children[0] = node;
				current.size = 1;
				current = node;
			} else {
				int idx = Arrays.binarySearch(current.labels, 0, current.size, c);
				if (idx >= 0) {
					current = current.children[idx];
				} else {
					if (current.size == current.labels.length) {
						// resize
						int newCapacity = (int) Math.ceil(current.size * trie.growthFactor);
						current.children = Arrays.copyOf(current.children, newCapacity);
						current.labels = Arrays.copyOf(current.labels, newCapacity);
					}
					idx = -idx - 1; // insertion point
					for (int i = current.size; i > idx; --i) {
						current.labels[i] = current.labels[i-1];
						current.children[i] = current.children[i-1];
					}
					current.labels[idx] = c;
					SimpleTrieNode<T> node = new SimpleTrieNode<T>();
					current.children[idx] = node;
					++current.size;
					current = node;
				}
			}
		}

		@Override
		public void removeChild(char c) {
			int idx = Arrays.binarySearch(current.labels, 0, current.size, c);
			if (idx >= 0) {
				--current.size;
				for (int i = idx; i < current.size; ++i) {
					current.labels[i] = current.labels[i+1];
					current.children[i] = current.children[i+1];
				}
			}
		}

		@Override
		public boolean moveToParent() {
			if (parents.isEmpty()) {
				return false;
			} else {
				current = parents.pop();
				return true;
			}
		}

		@Override
		public void getChildren(CharArrayList children) {
			for (int i = 0; i < current.size; ++i) {
				children.add(current.labels[i]);
			}
		}

		@Override
		public T getValue() {
			return current.value;
		}

		@Override
		public void setValue(T value) {
			current.value = value;
		}

		@Override
		public void reset() {
			current = trie.root;
			parents.clear();
		}

		@Override
		public int size() {
			return current.size();
		}

		public SimpleTrieCursor<T> clone() {
			return new SimpleTrieCursor<T>(trie, current,
					new ArrayDeque<SimpleTrieNode<T>>(parents));
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
		validate(initialCapacity, growthFactor);
		this.initialCapacity = initialCapacity;
		this.growthFactor = growthFactor;
		root = new SimpleTrieNode<T>();
	}

	public SimpleTrie() {
		this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR);
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