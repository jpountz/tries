package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

import net.jpountz.trie.util.PrefixedCharSequence;
import net.jpountz.trie.util.Utils;

/**
 * Simple implementation where each node stores its children sorted
 * lexicographically.
 */
public final class SimpleTrie<T> extends AbstractTrie<T> {

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

	private static final class SimpleTrieCursor<T> implements Cursor<T> {

		protected final SimpleTrie<T> trie;
		protected SimpleTrieNode<T> current;
		protected final Deque<SimpleTrieNode<T>> parents;

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
		public void getChildrenLabels(CharArrayList children) {
			for (int i = 0; i < current.size; ++i) {
				children.add(current.labels[i]);
			}
		}

		@Override
		public void getChildren(Char2ObjectMap<Cursor<T>> children) {
			for (int i = 0; i < current.size; ++i) {
				char c = current.labels[i];
				SimpleTrieNode<T> child = current.children[i];
				SimpleTrieCursor<T> cursor = this.clone();
				cursor.current = child;
				cursor.parents.push(current);
				children.put(c, cursor);
			}
		}

		private static class SuffixIterable<T> implements Iterable<Entry<T>> {

			private final SimpleTrieNode<T> root;
			private final CharSequence prefix;

			public SuffixIterable(SimpleTrieNode<T> root, CharSequence prefix) {
				this.root = root;
				this.prefix = prefix;
			}

			@Override
			public Iterator<Entry<T>> iterator() {
				if (root.size == 0) {
					if (root.value == null) {
						return Collections.<Entry<T>>emptySet().iterator();
					} else {
						return Collections.singleton(
								EntryImpl.newInstance(prefix, root.value)).iterator();
					}
				} else {
					int size = root.size;
					if (root.value != null) {
						++size;
					}
					Collection<Iterator<Entry<T>>> iterators = new ArrayList<Iterator<Entry<T>>>(size);
					if (root.value != null) {
						iterators.add(Collections.singleton(
								EntryImpl.newInstance(prefix, root.value)).iterator());
					}
					for (int i = 0; i < root.size; ++i) {
						char c = root.labels[i];
						SimpleTrieNode<T> node = root.children[i];
						iterators.add(new SuffixIterable<T>(node, new PrefixedCharSequence(prefix, c)).iterator());
					}
					return Utils.concat(iterators.iterator());
				}
			}

		}

		@Override
		public Iterable<Entry<T>> getSuffixes() {
			return new SuffixIterable<T>(current, "");
		}

		public boolean moveToParent() {
			if (parents.isEmpty()) {
				return false;
			} else {
				current = parents.pop();
				return true;
			}
		}

		public T getValue() {
			return current.value;
		}

		public void setValue(T value) {
			current.value = value;
		}

		public int size() {
			return current.size();
		}

		public void reset() {
			current = trie.root;
			parents.clear();
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
		root = new SimpleTrieNode<T>();
		validate(initialCapacity, growthFactor);
		this.initialCapacity = initialCapacity;
		this.growthFactor = growthFactor;
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

}