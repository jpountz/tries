package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;

import java.util.ArrayDeque;
import java.util.Deque;

import net.jpountz.trie.util.FastCharMap;

/**
 * Trie based on a {@link FastCharMap}.
 */
public final class FastCharMapTrie<T> extends AbstractNodeTrie<T> {

	private static final class FastCharMapTrieNode<T> extends AbstractNodeTrieNode<T> {

		FastCharMap<FastCharMapTrieNode<T>> children;

		public FastCharMapTrieNode(char label) {
			super(label);
		}

		@Override
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

		@Override
		protected AbstractNodeTrieNode<T> getFirstChild() {
			if (children != null) {
				char c = children.nextKey('\0');
				if (c != '\0') {
					FastCharMapTrieNode<T> child = children.get(c);
					return child;
				}
			}
			return null;
		}

		@Override
		protected AbstractNodeTrieNode<T> getChild(char c) {
			if (children != null) {
				return children.get(c);
			}
			return null;
		}

		@Override
		protected AbstractNodeTrieNode<T> addChild(char c) {
			if (children != null) {
				FastCharMapTrieNode<T> child = children.get(c);
				if (child != null) {
					return child;
				}
			} else {
				children = new FastCharMap<FastCharMapTrieNode<T>>();
			}
			FastCharMapTrieNode<T> child = new FastCharMapTrieNode<T>(c);
			children.put(c, child);
			char prevKey = children.prevKey(c);
			if (prevKey != '\0') {
				children.get(prevKey).brother = child;
			}
			char nextKey = children.nextKey(c);
			if (nextKey != '\0') {
				child.brother = children.get(nextKey);
			}
			return child;
		}

		@Override
		protected boolean removeChild(char c) {
			if (children.remove(c)) {
				char prevKey = children.prevKey(c);
				if (prevKey != '\0') {
					char nextKey = children.nextKey(c);
					if (nextKey != '\0') {
						 children.get(prevKey).brother = children.get(nextKey);
					} else {
						children.get(prevKey).brother = null;
					}
				}
			}
			return false;
		}

		@Override
		protected void getChildrenLabels(CharCollection childrenCollection) {
			if (children != null) {
				char c = '\0';
				while ((c = children.nextKey(c)) != '\0') {
					childrenCollection.add(c);
				}
			}
		}

		@Override
		protected int childrenSize() {
			return children.size();
		}
	}

	private static class FastCharMapTrieCursor<T> extends AbstractNodeTrieCursor<T> {

		private FastCharMapTrieCursor(FastCharMapTrie<T> trie,
				FastCharMapTrieNode<T> current,
				Deque<AbstractNodeTrieNode<T>> parents, StringBuilder label) {
			super(trie, current, parents, label);
		}

		public FastCharMapTrieCursor(FastCharMapTrie<T> trie) {
			this(trie, trie.root, new ArrayDeque<AbstractNodeTrieNode<T>>(),
					new StringBuilder());
		}

		public FastCharMapTrieCursor<T> clone() {
			return new FastCharMapTrieCursor<T>((FastCharMapTrie<T>) trie,
					(FastCharMapTrieNode<T>) current,
					new ArrayDeque<AbstractNodeTrieNode<T>>(parents),
					new StringBuilder(label));
		}
	}

	private final FastCharMapTrieNode<T> root;

	public FastCharMapTrie() {
		root = new FastCharMapTrieNode<T>('\0');
	}

	@Override
	protected AbstractNodeTrieNode<T> getRoot() {
		return root;
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
