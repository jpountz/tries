package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;

import java.util.Map;

/**
 * Fake trie implementation based on a Map for lookup performance comparison.
 */
public abstract class MapTrie<T> extends AbstractTrie<T> {

	private static class MapTrieCursor<T> extends AbstractCursor<T> {

		private final MapTrie<T> trie;
		private final /*MutableString*/StringBuilder prefix;

		public MapTrieCursor(MapTrie<T> trie, /*MutableString*/StringBuilder prefix) {
			this.trie = trie;
			this.prefix = prefix;
		}

		public MapTrieCursor(MapTrie<T> trie) {
			this(trie, new StringBuilder());
		}

		@Override
		protected CharSequence getLabelInternal() {
			return prefix;
		}

		@Override
		public Node getNode() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isAtRoot() {
			return prefix.length() == 0;
		}

		@Override
		public boolean isAt(Node node) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getLabel() {
			return prefix.toString();
		}

		@Override
		public int depth() {
			return prefix.length();
		}

		@Override
		public char getEdgeLabel() {
			return prefix.charAt(prefix.length() - 1);
		}

		@Override
		public boolean moveToChild(char c) {
			prefix.append(c);
			return true;
		}

		@Override
		public boolean moveToFirstChild() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean moveToBrother() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addChild(char c) {
			prefix.append(c);
		}

		@Override
		public boolean removeChild(char c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeChildren() {
			throw new UnsupportedOperationException();			
		}

		@Override
		public boolean moveToParent() {
			if (prefix.length() == 0) {
				return false;
			} else {
				prefix.setLength(prefix.length() - 1);
				return true;
			}
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getChildrenSize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public T getValue() {
			return trie.map.get(prefix);
		}

		@Override
		public void setValue(T value) {
			trie.put(prefix, value);
		}

		@Override
		public void reset() {
			prefix.setLength(0);
		}

		@Override
		public int size() {
			throw new UnsupportedOperationException();
		}

		public MapTrieCursor<T> clone() {
			return new MapTrieCursor<T>(trie, new StringBuilder(prefix));
		}

	}

	final Map</*Mutable*/String, T> map;

	public MapTrie() {
		map = getMap();
	}

	protected abstract Map<String, T> getMap();

	@Override
	public Cursor<T> getCursor() {
		return new MapTrieCursor<T>(this);
	}

	@Override
	public void put(char[] buffer, int offset, int length, T value) {
		map.put(new String(buffer, offset, length), value);
	}

	public void put(CharSequence sequence, int offset, int length, T value) {
		String key = sequence.toString();
		if (offset > 0 || length != sequence.length()) {
			key = key.substring(offset, length);
		}
		map.put(key, value);
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		return map.get(new String(buffer, offset, length));
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		String key = sequence.toString();
		if (offset > 0 || length != sequence.length()) {
			key = key.substring(offset, length);
		}
		return map.get(key);
	}

	@Override
	public void clear() {
		map.clear();
	}

}
