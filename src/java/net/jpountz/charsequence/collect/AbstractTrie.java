package net.jpountz.charsequence.collect;

import it.unimi.dsi.fastutil.chars.CharCollection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Base implementation for tries. By default, removals are performed by putting
 * null.
 */
abstract class AbstractTrie<T> extends AbstractCharSequenceMap<T>
		implements Trie<T> {

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

	}

	public T put(char[] buffer, int offset, int length, T value) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			cursor.addChild(buffer[offset+i]);
		}
		T result = cursor.getValue();
		cursor.setValue(value);
		return result;
	}

	public T put(CharSequence sequence, int offset, int length, T value) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			cursor.addChild(sequence.charAt(offset+i));
		}
		T result = cursor.getValue();
		cursor.setValue(value);
		return result;
	}

	public T remove(char[] buffer, int offset, int length) {
		if (get(buffer, offset, length) != null) {
			return put(buffer, offset, length, null);
		} else {
			return null;
		}
	}

	public T remove(char[] buffer) {
		return remove(buffer, 0, buffer.length);
	}

	public T remove(CharSequence sequence, int offset, int length) {
		if (get(sequence, offset, length) != null) {
			return put(sequence, offset, length, null);
		} else {
			return null;
		}
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

	public T get(CharSequence sequence, int offset, int length) {
		Cursor<T> cursor = getCursor();
		for (int i = 0; i < length; ++i) {
			if (!cursor.moveToChild(sequence.charAt(offset+i))) {
				return null;
			}
		}
		return cursor.getValue();
	}

	public int size() {
		Cursor<T> cursor = getCursor();
		Node root = cursor.getNode();
		int result = 0;
		do {
			if (cursor.getValue() != null) {
				++result;
			}
		} while (Tries.moveToNextSuffix(root, cursor, Trie.Traversal.DEPTH_FIRST));
		return result;
	}

	@Override
	public boolean isEmpty() {
		return get("") == null && !getCursor().moveToFirstChild();
	}

	@Override
	public Set<Map.Entry<String, T>> entrySet() {
		return new AbstractSet<Map.Entry<String,T>>() {

			@Override
			public Iterator<Map.Entry<String, T>> iterator() {
				return new Iterator<Map.Entry<String, T>>() {

					private Cursor<T> cursor;
					private final Node root;
					private boolean isAtNext;

					{
						isAtNext = false;
						cursor = AbstractTrie.this.getCursor();
						root = cursor.getNode();
						isAtNext = cursor.getValue() != null;
					}

					private boolean moveToNext() {
						if (cursor == null) {
							return false;
						}
						while (Tries.moveToNextSuffix(root, cursor, Trie.Traversal.DEPTH_FIRST)) {
							if (cursor.getValue() != null) {
								return true;
							}
						}
						if (cursor.isAt(root)) {
							cursor = null;
						}
						return false;
					}

					@Override
					public boolean hasNext() {
						if (!isAtNext && moveToNext()) {
							isAtNext = true;
						}
						return isAtNext;
					}

					@Override
					public java.util.Map.Entry<String, T> next() {
						if (isAtNext || (cursor != null && moveToNext())) {
							isAtNext = false;
							return new AbstractMap.SimpleImmutableEntry<String, T>(cursor.getLabel(), cursor.getValue());
						}
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
				};
			}

			@Override
			public int size() {
				return AbstractTrie.this.size();
			}
		};
	}

	@Override
	public int nodes() {
		int result = 1;
		Cursor<T> cursor = getCursor();
		Node root = cursor.getNode();
		while (Traversal.DEPTH_FIRST.moveToNextNode(root, cursor)) {
			++result;
		}
		return result;
	}
}
