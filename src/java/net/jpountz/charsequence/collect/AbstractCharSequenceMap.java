package net.jpountz.charsequence.collect;

import java.util.AbstractMap;

/**
 * Abstract implementation for a {@link CharSequenceMap}.
 *
 * @param <V> the type of the values contained in this map.
 */
public abstract class AbstractCharSequenceMap<V>
		extends AbstractMap<String, V>
		implements CharSequenceMap<V> {

	@Override
	public V get(Object key) {
		if (key instanceof char[]) {
			return get((char[]) key);
		} else if (key instanceof CharSequence) {
			return get((CharSequence) key);
		} else {
			return null;
		}
	}

	public V put(String key, V value) {
		return put(key, 0, key.length(), value);
	}

	@Override
	public final V get(CharSequence key) {
		return get(key, 0, key.length());
	}

	@Override
	public final V get(char[] key) {
		return get(key, 0, key.length);
	}

	@Override
	public boolean containsKey(CharSequence key, int offset, int length) {
		return get(key, offset, length) != null;
	}

	@Override
	public final boolean containsKey(CharSequence key) {
		return containsKey(key, 0, key.length());
	}

	@Override
	public boolean containsKey(char[] key, int offset, int length) {
		return get(key, offset, length) != null;
	}

	@Override
	public final boolean containsKey(char[] key) {
		return containsKey(key, 0, key.length);
	}

	@Override
	public final V put(CharSequence key, V value) {
		return put(key, 0, key.length(), value);
	}

	@Override
	public final V put(char[] key, V value) {
		return put(key, 0, key.length, value);
	}

	@Override
	public V remove(CharSequence key, int offset, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final V remove(CharSequence key) {
		return remove(key, 0, key.length());
	}

	@Override
	public V remove(char[] key, int offset, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(char[] key) {
		return remove(key, 0, key.length);
	}

}
