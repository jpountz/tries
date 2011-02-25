package net.jpountz.trie;

import java.util.ArrayDeque;
import java.util.Arrays;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class StringArrayTrie<T> extends AbstractTrie<T> {

	private static class StringArrayTrieNode implements Node {

		final char[] prefix; 
		final int position;

		public StringArrayTrieNode(int position, StringBuilder prefix) {
			this.position = position;
			int length = prefix.length();
			this.prefix = new char[length];
			prefix.getChars(0, length, this.prefix, 0);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + position;
			result = prime * result + Arrays.hashCode(prefix);
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StringArrayTrieNode other = (StringArrayTrieNode) obj;
			if (position != other.position)
				return false;
			if (!Arrays.equals(prefix, other.prefix))
				return false;
			return true;
		}

	}

	private static class StringArrayTrieCursor<T> extends AbstractCursor<T> {

		final StringArrayTrie<T> trie;
		int current;
		final IntArrayList parents;
		final StringBuilder prefix;

		private StringArrayTrieCursor(StringArrayTrie<T> trie, int current,
				IntArrayList parents, StringBuilder label, StringBuilder prefix) {
			super(label);
			this.trie = trie;
			this.current = current;
			this.parents = parents;
			this.prefix = prefix;
		}

		public StringArrayTrieCursor(StringArrayTrie<T> trie) {
			this(trie, START, new IntArrayList(), new StringBuilder(), new StringBuilder());
		}

		@Override
		public Node getNode() {
			return new StringArrayTrieNode(current, prefix);
		}

		@Override
		public boolean moveToChild(char c) {
			return false;
		}

		@Override
		public boolean moveToFirstChild() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToBrother() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void addChild(char c) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean removeChild(char c) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToParent() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getChildrenSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public T getValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setValue(T value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public net.jpountz.trie.AbstractTrie.AbstractCursor<T> clone() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public static final int START = 0;
	public static final int NOT_FOUND = -1;

	private static final int DEFAULT_CAPACITY = 255;
	private static final float DEFAULT_GROWTH_FACTOR = 2f;

	private final float growthFactor;
	private int capacity;
	private int size;

	private int[] children;
	private int[] brothers;
	private char[][] labels;
	private Object[] values;

	public StringArrayTrie(int initialCapacity, float growthFactor) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException("initialCapacity must be > 1");
		} else if (growthFactor <= 1) {
			throw new IllegalArgumentException("growthFactor must be > 1");
		}
		this.capacity = initialCapacity;
		this.growthFactor = growthFactor;
		size = 1;
		children = new int[capacity];
		Arrays.fill(children, NOT_FOUND);
		brothers = new int[capacity];
		Arrays.fill(brothers, NOT_FOUND);
		labels = new char[capacity][];
		values = new Object[capacity];
	}

	@Override
	public Cursor<T> getCursor() {
		return new StringArrayTrieCursor<T>(this);
	}

	@Override
	public void clear() {
		
	}

}
