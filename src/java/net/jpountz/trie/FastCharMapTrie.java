package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.ArrayDeque;
import java.util.Deque;

import net.jpountz.trie.util.FastCharMap;

/**
 * Trie based on a {@link FastCharMap}.
 */
public final class FastCharMapTrie<T> extends AbstractTrie<T> {

	private static final class FastCharMapTrieNode<T> implements Node {

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

	private static class FastCharMapTrieCursor<T> extends AbstractCursor<T> {

		final FastCharMapTrie<T> trie;
		FastCharMapTrieNode<T> current;
		final Deque<FastCharMapTrieNode<T>> parents;

		private FastCharMapTrieCursor(FastCharMapTrie<T> trie, FastCharMapTrieNode<T> current,
				Deque<FastCharMapTrieNode<T>> parents, StringBuilder label) {
			super(label);
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		public FastCharMapTrieCursor(FastCharMapTrie<T> trie) {
			this(trie, trie.root, new ArrayDeque<FastCharMapTrieNode<T>>(), new StringBuilder());
		}

		@Override
		public Node getNode() {
			return current;
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
					label.append(c);
					return true;
				}
			} else {
				return false;
			}
		}

		@Override
		public boolean moveToFirstChild() {
			if (current.children != null) {
				char c = current.children.nextKey('\0');
				if (c != '\0') {
					FastCharMapTrieNode<T> child = current.children.get(c);
					parents.push(current);
					current = child;
					label.append(c);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean moveToBrother() {
			char c = getEdgeLabel();
			if (moveToParent()) {
				char next = current.children.nextKey(c);
				final boolean result = next != '\0';
				if (result) {
					c = next;
				}
				FastCharMapTrieNode<T> child = current.children.get(c);
				if (child == null) {
					throw new IllegalStateException("A portion of the trie where the cursor was has been removed");
				}
				parents.push(current);
				current = child;
				label.append(c);
				return result;
			}
			return false;
		}

		@Override
		public void addChild(char c) {
			parents.push(current);
			label.append(c);
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
		public int getChildrenSize() {
			return current.children.size();
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

		@Override
		public FastCharMapTrieCursor<T> clone() {
			return new FastCharMapTrieCursor<T>(trie, current,
					new ArrayDeque<FastCharMapTrieNode<T>>(parents),
					new StringBuilder(label));
		}

		public boolean moveToParent() {
			if (parents.isEmpty()) {
				return false;
			} else {
				current = parents.pop();
				label.setLength(label.length() - 1);
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
			label.setLength(0);
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
