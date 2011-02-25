package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;

import java.util.Deque;

public abstract class AbstractNodeTrie<T> extends AbstractTrie<T> {

	protected static abstract class AbstractNodeTrieNode<T> implements Node {

		protected T value;
		protected final char label;
		AbstractNodeTrieNode<T> brother;

		public AbstractNodeTrieNode(char label) {
			this.label = label;
		}

		protected abstract AbstractNodeTrieNode<T> getFirstChild();
		protected abstract AbstractNodeTrieNode<T> getChild(char c);
		protected abstract AbstractNodeTrieNode<T> addChild(char c);
		protected abstract boolean removeChild(char c);
		protected abstract void getChildrenLabels(CharCollection children);
		protected abstract int size();
		protected abstract int childrenSize();
	}

	protected static abstract class AbstractNodeTrieCursor<T> extends AbstractCursor<T> {

		protected final AbstractNodeTrie<T> trie;
		protected AbstractNodeTrieNode<T> current;
		protected final Deque<AbstractNodeTrieNode<T>> parents;

		protected AbstractNodeTrieCursor(AbstractNodeTrie<T> trie, AbstractNodeTrieNode<T> current,
				Deque<AbstractNodeTrieNode<T>> parents, StringBuilder label) {
			super(label);
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		@Override
		public final Node getNode() {
			return current;
		}

		@Override
		public final boolean moveToChild(char c) {
			AbstractNodeTrieNode<T> child = current.getChild(c);
			if (child == null) {
				return false;
			} else {
				parents.push(current);
				label.append(c);
				current = child;
				return true;
			}
		}

		@Override
		public final boolean moveToFirstChild() {
			AbstractNodeTrieNode<T> firstChild = current.getFirstChild();
			if (firstChild == null) {
				return false;
			} else {
				parents.push(current);
				label.append(firstChild.label);
				current = firstChild;
				return true;
			}
		}

		@Override
		public final boolean moveToBrother() {
			if (current.brother != null) {
				current = current.brother;
				label.setCharAt(label.length() - 1, current.label);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public final void addChild(char c) {
			parents.push(current);
			label.append(c);
			current = current.addChild(c);
		}

		@Override
		public final boolean removeChild(char c) {
			return current.removeChild(c);
		}

		public final boolean moveToParent() {
			if (parents.isEmpty()) {
				return false;
			} else {
				current = parents.pop();
				label.setLength(label.length() - 1);
				return true;
			}
		}

		public final T getValue() {
			return current.value;
		}

		public final void setValue(T value) {
			current.value = value;
		}

		@Override
		public final void getChildrenLabels(CharCollection children) {
			current.getChildrenLabels(children);
		}

		@Override
		public final int getChildrenSize() {
			return current.childrenSize();
		}

		public final int size() {
			return current.size();
		}

		public void reset() {
			current = trie.getRoot();
			parents.clear();
			label.setLength(0);
		}

		public abstract AbstractNodeTrieCursor<T> clone();
	}

	abstract AbstractNodeTrieNode<T> getRoot();

	@Override
	public int size() {
		return getRoot().size();
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		AbstractNodeTrieNode<T> node = getRoot();
		for (int i = 0; i < length; ++i) {
			node = node.getChild(buffer[offset+i]);
			if (node == null) {
				return null;
			}
		}
		return node.value;
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		AbstractNodeTrieNode<T> node = getRoot();
		for (int i = 0; i < length; ++i) {
			node = node.getChild(sequence.charAt(offset+i));
			if (node == null) {
				return null;
			}
		}
		return node.value;
	}

	@Override
	public void put(char[] buffer, int offset, int length, T value) {
		AbstractNodeTrieNode<T> node = getRoot();
		for (int i = 0; i < length; ++i) {
			node = node.addChild(buffer[offset+i]);
		}
		node.value = value;
	}

	@Override
	public void put(CharSequence sequence, int offset, int length, T value) {
		AbstractNodeTrieNode<T> node = getRoot();
		for (int i = 0; i < length; ++i) {
			node = node.addChild(sequence.charAt(offset+i));
		}
		node.value = value;
	}
}
