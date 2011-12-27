package net.jpountz.charsequence.collect;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.jpountz.charsequence.Hash;
import net.jpountz.charsequence.StringHash;

/**
 * An open-addressing hashtable.
 */
public class CharArrayHashMap<V> extends AbstractCharSequenceMap<V> {

	private static final int DEFAULT_INITIAL_CAPACITY = 16;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private char[][] keys;
	private Object[] values;
	private final Hash hash;
	private final float loadFactor;
	private int size;
	private int threshold;
	private int mask;

	public CharArrayHashMap() {
		this.hash = StringHash.INSTANCE;
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		keys = new char[DEFAULT_INITIAL_CAPACITY][];
		values = new Object[DEFAULT_INITIAL_CAPACITY];
		mask = keys.length - 1;
		threshold = (int) (loadFactor * keys.length);

	}

	private int indexFor(CharSequence sequence, int offset, int length) {
		return hash.hash(sequence, offset, length) & mask;
	}

	private int indexFor(char[] sequence, int offset, int length) {
		return hash.hash(sequence, offset, length) & mask;
	}

	private static boolean equals(char[] s, CharSequence key, int offset,
			int length) {
		if (length == s.length) {
			int o = offset;
			for (int i = 0; i < length; ++i) {
				if (s[i] != key.charAt(o++)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static boolean equals(char[] s, char[] key, int offset, int length) {
		if (length == s.length) {
			int o = offset;
			for (int i = 0; i < length; ++i) {
				if (s[i] == key[o++]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(CharSequence key, int offset, int length) {
		int start = indexFor(key, offset, length);
		for (int i = start; keys[i] != null; i = (i + 1) & mask) {
			if (equals(keys[i], key, offset, length)) {
				return (V) values[i];
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(char[] key, int offset, int length) {
		int start = indexFor(key, offset, length);
		for (int i = start; keys[i] != null; i = (i + 1) & mask) {
			if (equals(keys[i], key, offset, length)) {
				return (V) values[i];
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void ensureCapacity() {
		if (size >= threshold) {
			char[][] oldKeys = keys;
			Object[] oldValues = values;
			int newLength = values.length * 2;
			mask = newLength - 1;
			keys = new char[newLength][];
			values = new Object[newLength];
			for (int i = 0; i < oldKeys.length; ++i) {
				if (oldKeys[i] != null) {
					put(oldKeys[i], (V) oldValues[i]);
				}
			}
			threshold = (int) (loadFactor * keys.length);
		}
	}

	@Override
	public V put(CharSequence key, int offset, int length, V value) {
		ensureCapacity();
		int i = indexFor(key, offset, length);
		for (; keys[i] != null; i = (i + 1) & mask) {
			if (equals(keys[i], key, offset, length)) {
				break;
			}
		}
		if (keys[i] == null) {
			keys[i] = new char[key.length()];
			if (key instanceof String) {
				((String) key).getChars(offset, length, keys[i], 0);
			} else {
				int o = offset;
				for (int k = 0; k < length; ++k) {
					keys[i][k] = key.charAt(o++);
				}
			}
			++size;
		}
		@SuppressWarnings("unchecked")
		V result = (V) values[i];
		values[i] = value;
		return result;
	}

	@Override
	public V put(char[] key, int offset, int length, V value) {
		ensureCapacity();
		int i = indexFor(key, offset, length);
		for (; keys[i] != null; i = (i + 1) & mask) {
			if (equals(keys[i], key, offset, length)) {
				break;
			}
		}
		if (keys[i] == null) {
			keys[i] = Arrays.copyOfRange(key, offset, offset + length);
			++size;
		}
		@SuppressWarnings("unchecked")
		V result = (V) values[i];
		values[i] = value;
		return result;
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		return new AbstractSet<Map.Entry<String, V>>() {

			@Override
			public Iterator<java.util.Map.Entry<String, V>> iterator() {
				return new Iterator<java.util.Map.Entry<String, V>>() {

					private int current;
					private int next;

					{
						current = -1;
						next = -1;
						setNext();
					}

					private boolean setNext() {
						if (next < keys.length) {
							do {
								++next;
							} while (next < keys.length && keys[next] == null);
						}

						return next < keys.length;
					}

					@Override
					public boolean hasNext() {
						return (next != current && next < keys.length)
								|| setNext();
					}

					@SuppressWarnings("unchecked")
					@Override
					public java.util.Map.Entry<String, V> next() {
						if (hasNext()) {
							current = next;
							return new AbstractMap.SimpleImmutableEntry<String, V>(
									new String(keys[current]),
									(V) values[current]);
						}
						return null;
					}

					@Override
					public void remove() {
						if (current < 0) {
							throw new IllegalStateException("Not positioned");
						}
						keys[current] = null;
						values[current] = null;
					}
				};
			}

			@Override
			public int size() {
				return CharArrayHashMap.this.size();
			}

		};
	}

}
