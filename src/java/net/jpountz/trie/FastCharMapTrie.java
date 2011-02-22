package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

import net.jpountz.trie.util.FastCharMap;
import net.jpountz.trie.util.PrefixedCharSequence;
import net.jpountz.trie.util.Utils;

/**
 * Trie based on a {@link FastCharMap}.
 */
public final class FastCharMapTrie<T> extends AbstractTrie<T> {

	private static final class FastCharMapTrieNode<T> {

		FastCharMap<FastCharMapTrieNode<T>> children;
		T value;

		public int size() {
			int size = 1;
			if (children != null) {
				char c = '\0';
				while ((c = children.nextKey(c)) != '\0') {
					FastCharMapTrieNode<T> child = children.get(c);
					size += child.size();
				}
			}
			return size;
		}
	}

	private static class FastCharMapTrieCursor<T> implements Cursor<T> {

		final FastCharMapTrie<T> trie;
		FastCharMapTrieNode<T> current;
		final Deque<FastCharMapTrieNode<T>> parents;

		private FastCharMapTrieCursor(FastCharMapTrie<T> trie, FastCharMapTrieNode<T> current,
				Deque<FastCharMapTrieNode<T>> parents) {
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		public FastCharMapTrieCursor(FastCharMapTrie<T> trie) {
			this(trie, trie.root, new ArrayDeque<FastCharMapTrieNode<T>>());
		}

		@Override
		public boolean moveToChild(char c) {
			if (current.children != null) {
				FastCharMapTrieNode<T> child = current.children.get(c);
				if (child == null) {
					return false;
				} else {
					parents.push(current);
					current = child;
					return true;
				}
			} else {
				return false;
			}
		}

		@Override
		public void addChild(char c) {
			parents.push(current);
			FastCharMapTrieNode<T> child;
			if (current.children == null) {
				current.children = new FastCharMap<FastCharMapTrieNode<T>>();
				child = new FastCharMapTrieNode<T>();
				current.children.put(c, child);
			} else {
				child = current.children.get(c);
				if (child == null) {
					child = new FastCharMapTrieNode<T>();
					current.children.put(c, child);
				}
			}
			current = child;
		}

		@Override
		public void removeChild(char c) {
			current.children.remove(c);
		}

		@Override
		public void getChildrenLabels(CharArrayList children) {
			if (current.children != null) {
				char c = '\0';
				while ((c = current.children.nextKey(c)) != '\0') {
					children.add(c);
				}
			}
		}

		@Override
		public void getChildren(Char2ObjectMap<Cursor<T>> children) {
			if (current.children != null) {
				char c = '\0';
				while ((c = current.children.nextKey(c)) != '\0') {
					FastCharMapTrieNode<T> child = current.children.get(c);
					FastCharMapTrieCursor<T> cursor = this.clone();
					cursor.current = child;
					cursor.parents.push(current);
					children.put(c, cursor);
				}
			}
		}

		private static class SuffixIterable<T> implements Iterable<Entry<T>> {

			private final FastCharMapTrieNode<T> root;
			private final CharSequence prefix;

			public SuffixIterable(FastCharMapTrieNode<T> root, CharSequence prefix) {
				this.root = root;
				this.prefix = prefix;
			}

			@Override
			public Iterator<Entry<T>> iterator() {
				if (root.children == null || root.children.isEmpty()) {
					if (root.value == null) {
						return Collections.<Entry<T>>emptySet().iterator();
					} else {
						return Collections.singleton(
								EntryImpl.newInstance(prefix, root.value)).iterator();
					}
				} else {
					int size = root.children.size();
					if (root.value != null) {
						++size;
					}
					Collection<Iterator<Entry<T>>> iterators = new ArrayList<Iterator<Entry<T>>>(size);
					if (root.value != null) {
						iterators.add(Collections.singleton(
								EntryImpl.newInstance(prefix, root.value)).iterator());
					}
					char c = '\0';
					while ((c = root.children.nextKey(c)) != '\0') {
						FastCharMapTrieNode<T> node = root.children.get(c);
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

		@Override
		public FastCharMapTrieCursor<T> clone() {
			return new FastCharMapTrieCursor<T>(trie, current,
					new ArrayDeque<FastCharMapTrieNode<T>>(parents));
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
	}

	private final FastCharMapTrieNode<T> root;

	public FastCharMapTrie() {
		root = new FastCharMapTrieNode<T>();
	}

	@Override
	public Cursor<T> getCursor() {
		return new FastCharMapTrieCursor<T>(this);
	}

	@Override
	public void clear() {
		root.value = null;
		root.children = null;
	}

}
