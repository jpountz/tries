package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Trie based on a {@link FastCharMap}.
 */
public class FastCharMapTrie<T> extends AbstractTrie<T> {

	private static class FastCharMapTrieNode<T> {
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

		private final FastCharMapTrie<T> trie;
		private FastCharMapTrieNode<T> current;
		private Deque<FastCharMapTrieNode<T>> parents;

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
			if (current.children != null) {
				char c = '\0';
				while ((c = current.children.nextKey(c)) != '\0') {
					children.add(c);
				}
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
			parents.clear();
			current = trie.root;
		}

		@Override
		public int size() {
			return current.size();
		}

		@Override
		public FastCharMapTrieCursor<T> clone() {
			return new FastCharMapTrieCursor<T>(trie, current,
					new ArrayDeque<FastCharMapTrieNode<T>>(parents));
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
		
	}

}
