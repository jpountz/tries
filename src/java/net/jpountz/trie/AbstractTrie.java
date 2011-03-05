package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;


/**
 * Base implementation for tries. By default, removals are performed by putting
 * null.
 */
abstract class AbstractTrie<T> implements Trie<T> {

	protected static abstract class AbstractCursor<T> implements Cursor<T> {

		protected abstract CharSequence getLabelInternal();

		@Override
		public String getLabel() {
			return getLabelInternal().toString();
		}

		@Override
		public int depth() {
			return getLabelInternal().length();
		}

		@Override
		public char getEdgeLabel() {
			CharSequence label = getLabelInternal();
			if (label.length() == 0) {
				return '\0';
			} else {
				return label.charAt(label.length()-1);
			}
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			if (moveToFirstChild()) {
				children.add(getEdgeLabel());
				while (moveToBrother()) {
					children.add(getEdgeLabel());
				}
				moveToParent();
			}
		}

		@Override
		public int getChildrenSize() {
			int result = 0;
			if (moveToFirstChild()) {
				++result;
				while (moveToBrother()) {
					++result;
				}
				moveToParent();
			}
			return result;
		}

		@Override
		public boolean isAtRoot() {
			return depth() == 0;
		}

		@Override
		public boolean isAt(Node under) {
			return getNode().equals(under);
		}

		@Override
		public int size() {
			Node node = getNode();
			int size = 1;
			while (Trie.Traversal.DEPTH_FIRST.moveToNextNode(node, this)) {
				++size;
			}
			return size;
		}

		public abstract AbstractCursor<T> clone();
	}

	@SuppressWarnings("unchecked")
	static <T> Cursor<T> getEmptyCursor() {
		return EMPTY_CURSOR;
	}

	@SuppressWarnings("rawtypes")
	static final Cursor EMPTY_CURSOR = new AbstractCursor() {

		protected CharSequence getLabelInternal() {
			return "";
		}

		@Override
		public Node getNode() {
			return null;
		}

		@Override
		public boolean isAtRoot() {
			return true;
		}

		@Override
		public boolean isAt(Node node) {
			return node == null;
		}

		@Override
		public int depth() {
			return 0;
		}

		@Override
		public String getLabel() {
			return "";
		}

		@Override
		public char getEdgeLabel() {
			return '\0';
		}

		@Override
		public boolean moveToChild(char c) {
			return false;
		}

		@Override
		public boolean moveToFirstChild() {
			return false;
		}

		@Override
		public boolean moveToBrother() {
			return false;
		}

		@Override
		public void addChild(char c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeChild(char c) {
			return false;
		}

		@Override
		public void removeChildren() {}

		@Override
		public boolean moveToParent() {
			return false;
		}

		@Override
		public void getChildrenLabels(CharCollection children) {}

		@Override
		public int getChildrenSize() {
			return 0;
		}

		@Override
		public Object getValue() {
			return null;
		}

		@Override
		public void setValue(Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {}

		@Override
		public int size() {
			return 0;
		}

		public AbstractCursor clone() {
			return this;
		}
	};

	static class EntryImpl<T> implements Entry<T>, Comparable<Entry<T>> {

		private final CharSequence key;
		private final T value;

		public EntryImpl(CharSequence key, T value) {
			this.key = key;
			this.value = value;
		}

		public static <T> Entry<T> newInstance(CharSequence key, T value) {
			return new EntryImpl<T>(key, value);
		}

		@Override
		public CharSequence getKey() {
			return key;
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public int compareTo(Entry<T> other) {
			CharSequence otherKey = other.getKey();
			int max = Math.max(key.length(), otherKey.length());
			int result = 0;
			for (int i = 0; i < max; ++i) {
				result = key.charAt(i) - otherKey.charAt(i);
				if (result != 0) {
					return result;
				}
			}
			return otherKey.length() - key.length();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntryImpl<?> other = (EntryImpl<?>) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return new StringBuilder(key).append(" --> ").append(value).toString();
		}

	}

	protected void validate(int initialCapacity, float growthFactor) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException("initialCapacity must be > 0");
		}
		if (growthFactor <= 1) {
			throw new IllegalArgumentException("growthFactor must be > 1");
		}
	}

	public void put(char[] buffer, int offset, int length, T value) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			cursor.addChild(buffer[offset+i]);
		}
		cursor.setValue(value);
	}

	public void put(char[] buffer, T value) {
		put(buffer, 0, buffer.length, value);
	}

	public void put(CharSequence sequence, int offset, int length, T value) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			cursor.addChild(sequence.charAt(offset+i));
		}
		cursor.setValue(value);
	}

	public void put(CharSequence sequence, T value) {
		put(sequence, 0, sequence.length(), value);
	}

	public void remove(char[] buffer, int offset, int length) {
		if (get(buffer, offset, length) != null) {
			put(buffer, offset, length, null);
		}
	}

	public void remove(char[] buffer) {
		remove(buffer, 0, buffer.length);
	}

	public void remove(CharSequence sequence, int offset, int length) {
		if (get(sequence, offset, length) != null) {
			put(sequence, offset, length, null);
		}
	}

	public void remove(CharSequence sequence) {
		remove(sequence, 0, sequence.length());
	}

	public T get(char[] buffer, int offset, int length) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			if (!cursor.moveToChild(buffer[offset+i])) {
				return null;
			}
		}
		return cursor.getValue();
	}

	public T get(char[] buffer) {
		return get(buffer, 0, buffer.length);
	}

	public T get(CharSequence sequence, int offset, int length) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			if (!cursor.moveToChild(sequence.charAt(offset+i))) {
				return null;
			}
		}
		return cursor.getValue();
	}

	public T get(CharSequence sequence) {
		return get(sequence, 0, sequence.length());
	}

	public int size() {
		return getCursor().size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 1 && get("") == null;
	}

}
