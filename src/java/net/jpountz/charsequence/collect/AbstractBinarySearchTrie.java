package net.jpountz.charsequence.collect;

import java.util.Arrays;
import java.util.List;

import net.jpountz.charsequence.CharComparator;

/**
 * Trie view of two key-value lists using binary search.
 *
 */
abstract class AbstractBinarySearchTrie<K, T> extends AbstractTrie<T> {

	protected static class BinarySearchTrieNode implements Node {
		final String key;
		BinarySearchTrieNode(String key) { this.key = key; }
		@Override
		public int hashCode() { return key.hashCode(); }
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BinarySearchTrieNode other = (BinarySearchTrieNode) obj;
			if (!key.equals(other.key))
				return false;
			return true;
		}
	}

	static class BinarySearchTrieCursor<K, T> extends AbstractCursor<T> {

		/**
		 * When comparing only one char, the cost of the binary search is
		 * higher than the cost of the comparison.
		 */
		private static final int BINARY_SEARCH_THRESHOLD = 1000;

		protected final AbstractBinarySearchTrie<K, T> trie;
		protected final StringBuilder label;
		private int[] parents;
		private int parentsSize;

		BinarySearchTrieCursor(AbstractBinarySearchTrie<K, T> trie) {
			this.trie = trie;
			this.label = new StringBuilder();
			parents = new int[30];
			parentsSize = 0;
			pushParents(0, trie.keys.size());
		}

		private void pushParents(int low, int high) {
			if (parentsSize + 2 > parents.length) {
				parents = Arrays.copyOf(parents, parents.length*2);
			}
			parents[parentsSize++] = low;
			parents[parentsSize++] = high;
		}

		private int peekLowParent() {
			return parents[parentsSize-2];
		}

		private int peekHighParent() {
			return parents[parentsSize-1];
		}

		private int peekHighGrandParent() {
			return parents[parentsSize-3];
		}

		private void clearParents() {
			parentsSize = 0;
			pushParents(0, trie.keys.size());
		}

		private void removeLastParents() {
			parentsSize -= 2;
		}

		@Override
		public Node getNode() {
			return new BinarySearchTrieNode(getLabel());
		}

		@Override
		public boolean isAt(Node node) {
			if (node instanceof BinarySearchTrieNode) {
				BinarySearchTrieNode n = (BinarySearchTrieNode) node;
				return trie.comparator.compare(label, n.key) == 0;
			}
			return false;
		}

		private int searchFirst(int offset, char c, int from, int to) {
			int lo = from, hi = to - 1;

			while (hi - lo >= BINARY_SEARCH_THRESHOLD) {
				int mid = (lo + hi) >>> 1;
				char midVal = trie.charAt(trie.keys.get(mid), offset);
				int cmp = trie.comparator.compare(c, midVal);

				if (cmp <= 0) {
					hi = mid;
				} else {
					lo = mid + 1;
				}
			}
			
			for (int i = lo; i <= hi; ++i) {
				if (trie.comparator.compare(c, trie.charAt(trie.keys.get(i), offset)) == 0) {
					return i;
				}
			}
			return -1;
		}

		private int searchLast(int offset, char c, int from, int to) {
			int lo = from, hi = to;
			
			while (hi - lo >= BINARY_SEARCH_THRESHOLD) {
				int mid = (lo + hi) >>> 1;
				char midVal = trie.charAt(trie.keys.get(mid), offset);
				int cmp = trie.comparator.compare(c, midVal);

				if (cmp < 0) {
					hi = mid;
				} else {
					lo = mid + 1;
				}
			}

			for (int i = lo; i < hi; ++i) {
				if (trie.comparator.compare(c, trie.charAt(trie.keys.get(i), offset)) < 0) {
					return i;
				}
			}
			return hi;
		}

		@Override
		public boolean moveToChild(char c) {
			int lo = peekLowParent();
			int hi = peekHighParent();

			if (lo < trie.keys.size() && trie.size(trie.keys.get(lo)) == depth()) {
				++lo;
			}

			if (lo < hi) {
				lo = searchFirst(depth(), c, lo, hi);
				if (lo >= 0) {
					hi = searchLast(depth(), c, lo+1, hi);
					label.append(c);
					pushParents(lo, hi);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean moveToFirstChild() {
			int lo = peekLowParent();
			int hi = peekHighParent();

			if (lo < trie.keys.size() && trie.size(trie.keys.get(lo)) == depth()) {
				++lo;
			}

			if (lo < hi) {
				char c = trie.charAt(trie.keys.get(lo), depth());
				hi = searchLast(depth(), c, lo+1, hi);
				label.append(c);
				pushParents(lo, hi);
				return true;
			}
			return false;
		}

		@Override
		public boolean moveToBrother() {
			if (depth() > 0) {
				int hi = peekHighParent();
				int hihi = peekHighGrandParent();

				if (hi < hihi) {
					int lo = hi;
					char c = trie.charAt(trie.keys.get(lo), label.length()-1);
					label.setCharAt(label.length()-1, c);
					hi = searchLast(label.length()-1, c, lo+1, hihi);
					removeLastParents();
					pushParents(lo, hi);
					return true;
				}
			}
			return false;
		}

		@Override
		public void addChild(char c) {
			throw new UnsupportedOperationException();
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
			if (depth() > 0) {
				label.setLength(label.length()-1);
				removeLastParents();
				return true;
			}
			return false;
		}

		@Override
		public T getValue() {
			if (trie.keys.size() > 0) {
				K key = trie.keys.get(peekLowParent());
				if (trie.compare(label, 0, label.length(), key, 0, trie.size(key)) == 0) {
					return trie.values.get(peekLowParent());
				}
			}
			return null;
		}

		@Override
		public void setValue(T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {
			clearParents();
			label.setLength(0);
		}

		@Override
		protected CharSequence getLabelInternal() {
			return label;
		}

	}

	final List<K> keys;
	final List<T> values;
	final CharComparator comparator;

	/**
	 * @param keys the keys, sorted lexicographically
	 * @param values the values
	 */
	AbstractBinarySearchTrie(List<K>keys, List<T> values, CharComparator comparator) {
		if (keys.size() != values.size()) {
			throw new IllegalArgumentException("keys and values don't have the same size");
		}
		this.keys = keys;
		this.values = values;
		if (comparator != null) {
			this.comparator = comparator;
		} else {
			this.comparator = CharComparator.DEFAULT;
		}
	}

	protected abstract int compare(char[] key1, int offset1, int length1, K key2, int offset2, int length2);
	protected abstract int compare(CharSequence key1, int offset1, int length1, K key2, int offset2, int length2);
	protected abstract int size(K key);
	protected abstract char charAt(K key, int offset);

	int binarySearch(CharSequence key, int offset, int length) {
		int lo = 0, hi = keys.size()-1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			K midVal = keys.get(mid);
			int cmp = compare(key, offset, length, midVal, 0, size(midVal));
			if (cmp < 0) {
				hi = mid - 1;
			} else if (cmp > 0) {
				lo = mid + 1;
			} else {
				return mid;
			}
		}
		return -(lo + 1);
	}

	int binarySearch(char[] key, int offset, int length) {
		int lo = 0, hi = keys.size()-1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			K midVal = keys.get(mid);
			int cmp = compare(key, offset, length, midVal, 0, size(midVal));
			if (cmp < 0) {
				hi = mid - 1;
			} else if (cmp > 0) {
				lo = mid + 1;
			} else {
				return mid;
			}
		}
		return -(lo + 1);
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		int i = binarySearch(buffer, offset, length);
		if (i >= 0) {
			return values.get(i);
		}
		return null;
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		int i = binarySearch(sequence, offset, length);
		if (i >= 0) {
			return values.get(i);
		}
		return null;
	}

	@Override
	public Cursor<T> getCursor() {
		return new BinarySearchTrieCursor<K, T>(this);
	}

}
