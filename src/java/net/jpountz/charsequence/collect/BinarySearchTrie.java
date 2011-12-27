package net.jpountz.charsequence.collect;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.jpountz.charsequence.CharComparator;
import net.jpountz.charsequence.CharSequenceUtils;

/**
 * Trie view of two key-value arrays using binary search.
 *
 */
class BinarySearchTrie<T> extends AbstractTrie<T> {

	static class BinarySearchTrieNode implements Node {
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

	static class BinarySearchTrieCursor<T> extends AbstractCursor<T> {

		private final BinarySearchTrie<T> trie;
		private final StringBuilder label;
		private final IntArrayList lowParents;
		private final IntArrayList highParents;

		BinarySearchTrieCursor(BinarySearchTrie<T> trie) {
			this.trie = trie;
			this.label = new StringBuilder();
			this.lowParents = new IntArrayList(); lowParents.push(0);
			this.highParents = new IntArrayList(); highParents.push(trie.keys.length);
		}

		@Override
		public Node getNode() {
			return new BinarySearchTrieNode(getLabel());
		}

		private int searchFirst(int offset, char c, int from, int to) {
			int lo = from, hi = to - 1;

			while (lo < hi) {
				int mid = (lo + hi) >>> 1;
				char midVal = trie.keys[mid][offset];
				int cmp = trie.comparator.compare(c, midVal);

				if (cmp <= 0) {
					hi = mid;
				} else {
					lo = mid + 1;
				}
			}
			if (trie.keys[lo][offset] == c) {
				return lo;
			} else {
				return -1;
			}
		}

		private int searchLast(int offset, char c, int from, int to) {
			int lo = from, hi = to;

			while (lo < hi) {
				int mid = (lo + hi) >>> 1;
				char midVal = trie.keys[mid][offset];
				int cmp = trie.comparator.compare(c, midVal);

				if (cmp < 0) {
					hi = mid;
				} else {
					lo = mid + 1;
				}
			}
			return hi;
		}

		@Override
		public boolean moveToChild(char c) {
			int lo = lowParents.peekInt(0);
			int hi = highParents.peekInt(0);

			if (lo < trie.keys.length && trie.keys[lo].length == depth()) {
				++lo;
			}

			if (lo < hi) {
				lo = searchFirst(depth(), c, lo, hi);
				if (lo >= 0) {
					hi = searchLast(depth(), c, lo, hi);
					label.append(c);
					lowParents.add(lo);
					highParents.add(hi);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean moveToFirstChild() {
			int lo = lowParents.peekInt(0);
			int hi = highParents.peekInt(0);

			if (lo < trie.keys.length && trie.keys[lo].length == depth()) {
				++lo;
			}

			if (lo < hi) {
				char c = trie.keys[lo][depth()];
				hi = searchLast(depth(), c, lo, hi);
				label.append(c);
				lowParents.add(lo);
				highParents.add(hi);
				return true;
			}
			return false;
		}

		private static boolean startsWith(char[] key, CharSequence label, int off, int len) {
			if (key.length >= label.length()) {
				for (int i = 0; i < len; ++i) {
					if (key[i] != label.charAt(off+i)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean moveToBrother() {
			if (depth() > 0) {
				int hi = highParents.peekInt(0);
				int hihi = highParents.peekInt(1);

				if (hi < hihi) {
					if (startsWith(trie.keys[hi], label, 0, label.length()-1)) {
						int lo = hi;
						char c = trie.keys[lo][label.length()-1];
						label.setCharAt(label.length()-1, c);
						hi = searchLast(label.length()-1, c, lo, hihi);
						lowParents.popInt();
						lowParents.push(lo);
						highParents.popInt();
						highParents.push(hi);
						return true;
					}
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
				lowParents.popInt();
				highParents.popInt();
				return true;
			}
			return false;
		}

		@Override
		public T getValue() {
			if (trie.keys.length > 0) {
				char[] key = trie.keys[lowParents.peekInt(0)];
				if (CharSequenceUtils.equals(key, 0, key.length, label, 0, label.length())) {
					return trie.values[lowParents.peekInt(0)];
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
			lowParents.clear();
			highParents.clear();
			lowParents.push(0);
			highParents.push(trie.keys.length);
			label.setLength(0);
		}

		@Override
		protected CharSequence getLabelInternal() {
			return label;
		}

	}

	final char[][] keys;
	final T[] values;
	private final CharComparator comparator;

	/**
	 * @param keys the keys, sorted lexicographically
	 * @param values the values
	 */
	public BinarySearchTrie(char[][] keys, T[] values, CharComparator comparator) {
		if (keys.length != values.length) {
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

	private int compare(char[] key1, int offset1, int length1, char[] key2, int offset2, int length2) {
		int min = Math.min(length1, length2);
		for (int i = 0; i < min; ++i) {
			int result = comparator.compare(key1[offset1 + i], key2[offset2 + i]);
			if (result != 0) {
				return result;
			}
		}
		return length1 - length2;
	}

	int binarySearch(char[] key, int offset, int length) {
		int lo = 0, hi = keys.length-1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			char[] midVal = keys[mid];
			int cmp = compare(key, offset, length, midVal, 0, midVal.length);
			if (cmp < 0) {
				hi = mid - 1;
			} else if (cmp > 0) {
				lo = mid + 1;
			} else {
				return mid;
			}
		}
		return -lo + 1;
	}

	private int compare(CharSequence key1, int offset1, int length1, char[] key2, int offset2, int length2) {
		int min = Math.min(length1, length2);
		for (int i = 0; i < min; ++i) {
			int result = comparator.compare(key1.charAt(offset1 + i), key2[offset2 + i]);
			if (result != 0) {
				return result;
			}
		}
		return length1 - length2;
	}

	int binarySearch(CharSequence key, int offset, int length) {
		int lo = 0, hi = keys.length-1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			char[] midVal = keys[mid];
			int cmp = compare(key, offset, length, midVal, 0, midVal.length);
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
			return values[i];
		}
		return null;
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		int i = binarySearch(sequence, offset, length);
		if (i >= 0) {
			return values[i];
		}
		return null;
	}

	@Override
	public Cursor<T> getCursor() {
		return new BinarySearchTrieCursor<T>(this);
	}

}
