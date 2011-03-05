package net.jpountz.trie;

import java.util.BitSet;

/**
 * Delegating trie whose get operations use a bloom filter to test the presence
 * of the key.
 *
 * @param <T> the value type
 */
public class BloomFilteredTrie<T> extends DelegatingTrie<T> {

	private static final class Cursor<T> extends DelegatingTrie.Cursor<T> {

		private BloomFilteredTrie<T> trie;

		public Cursor(BloomFilteredTrie<T> trie, Trie.Cursor<T> delegate) {
			super(delegate);
			this.trie = trie;
		}

		@Override
		public boolean removeChild(char c) {
			if (moveToChild(c)) {
				Node node = getNode();
				trie.setBloomFilter(this, node);
				moveToParent();
			}
			return super.removeChild(c);
		}

		public void setValue(T value) {
			super.setValue(value);
			String label = getLabel();
			trie.setModified(label, 0, label.length());
		}

	}

	private final BitSet bloomFilter;
	private final Hash hash;

	public BloomFilteredTrie(TrieFactory<T> factory, Hash hash, int bloomFilterSize) {
		super(factory.newTrie());
		this.hash = hash;
		this.bloomFilter = new BitSet(bloomFilterSize);
	}

	private void setModified(char[] buffer, int offset, int length) {
		int bit = Math.abs(hash.hash(buffer, offset, length) % bloomFilter.size());
		bloomFilter.set(bit);
	}

	private boolean maybeExists(char[] buffer, int offset, int length) {
		int bit = Math.abs(hash.hash(buffer, offset, length) % bloomFilter.size());
		return bloomFilter.get(bit);
	}

	private void setModified(CharSequence buffer, int offset, int length) {
		int bit = Math.abs(hash.hash(buffer, offset, length) % bloomFilter.size());
		bloomFilter.set(bit);
	}

	private boolean maybeExists(CharSequence buffer, int offset, int length) {
		int bit = Math.abs(hash.hash(buffer, offset, length) % bloomFilter.size());
		return bloomFilter.get(bit);
	}

	private void setBloomFilter(Trie.Cursor<T> cursor, Node node) {
		do {
			String label = cursor.getLabel();
			setModified(label, 0, label.length());
		} while (Tries.moveToNextSuffix(node, cursor, Trie.Traversal.DEPTH_FIRST));
	}

	public void resetBloomFilter() {
		bloomFilter.clear();
		Trie.Cursor<T> cursor = delegate.getCursor();
		Node node = cursor.getNode();
		setBloomFilter(cursor, node);
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		if (maybeExists(buffer, offset, length)) {
			return super.get(buffer, offset, length);
		}
		return null;
	}

	@Override
	public T get(char[] buffer) {
		if (maybeExists(buffer, 0, buffer.length)) {
			return super.get(buffer);
		}
		return null;
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		if (maybeExists(sequence, offset, length)) {
			return super.get(sequence, offset, length);
		}
		return null;
	}

	@Override
	public T get(CharSequence sequence) {
		if (maybeExists(sequence, 0, sequence.length())) {
			return super.get(sequence, 0, sequence.length());
		}
		return null;
	}

	@Override
	public void put(char[] buffer, int offset, int length, T value) {
		super.put(buffer, offset, length, value);
		setModified(buffer, offset, length);
	}

	@Override
	public void put(char[] buffer, T value) {
		super.put(buffer, value);
		setModified(buffer, 0, buffer.length);
	}

	@Override
	public void put(CharSequence sequence, int offset, int length, T value) {
		super.put(sequence, offset, length, value);
		setModified(sequence, offset, length);
	}

	@Override
	public void put(CharSequence sequence, T value) {
		super.put(sequence, value);
		setModified(sequence, 0, sequence.length());
	}

	@Override
	public void remove(char[] buffer, int offset, int length) {
		super.remove(buffer, offset, length);
		setModified(buffer, offset, length);
	}

	@Override
	public void remove(char[] buffer) {
		setModified(buffer, 0, buffer.length);
	}

	@Override
	public void remove(CharSequence sequence, int offset, int length) {
		super.remove(sequence, offset, length);
		setModified(sequence, offset, length);
	}

	@Override
	public void remove(CharSequence sequence) {
		super.remove(sequence);
		setModified(sequence, 0, sequence.length());
	}

	@Override
	public Trie.Cursor<T> getCursor() {
		return new Cursor<T>(this, delegate.getCursor());
	}

}
