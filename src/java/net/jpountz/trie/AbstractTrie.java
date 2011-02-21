package net.jpountz.trie;

/**
 * Base implementation for tries. By default, removals are performed by putting
 * null, and trimToSize is a no-op.
 */
public abstract class AbstractTrie<T> implements Trie<T> {

	public void put(char[] buffer, T value) {
		put(buffer, 0, buffer.length, value);
	}

	public void put(CharSequence sequence, T value) {
		put(sequence, 0, sequence.length(), value);
	}

	public void remove(char[] buffer, int offset, int length) {
		put(buffer, offset, length, null);
	}

	public void remove(char[] buffer) {
		remove(buffer, 0, buffer.length);
	}

	public void remove(CharSequence sequence, int offset, int length) {
		put(sequence, offset, length, null);
	}

	public void remove(CharSequence sequence) {
		remove(sequence, 0, sequence.length());
	}

	public T get(char[] buffer) {
		return get(buffer, 0, buffer.length);
	}

	public T get(CharSequence sequence) {
		return get(sequence, 0, sequence.length());
	}

	public void trimToSize() {
		// Do nothing
	}
}
