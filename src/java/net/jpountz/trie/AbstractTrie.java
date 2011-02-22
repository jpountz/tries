package net.jpountz.trie;

/**
 * Base implementation for tries. By default, removals are performed by putting
 * null, and trimToSize is a no-op.
 */
abstract class AbstractTrie<T> implements Trie<T> {

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

	public void trimToSize() {
		// Do nothing
	}

}
