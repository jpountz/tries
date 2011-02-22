package net.jpountz.trie;
import java.util.Arrays;

public class FastCharMap<V> {

	private char min;
	private Object[] values;
	private int size;

	public FastCharMap() {
		min = '\0';
	}

	@SuppressWarnings("unchecked")
	public V get(char c) {
		if (c >= min && values != null) {
			int offset = c - min;
			if (offset < values.length) {
				return (V) values[offset];
			}
		}
		return null;
	}

	public void remove(char c) {
		if (c > min && c < min + values.length - 1) {
			values[c-min] = null;
		} else if (c == min) {
			char newMin = nextKey(c);
			if (newMin == '\0') {
				values = null;
			} else {
				values = Arrays.copyOfRange(values, newMin - min, values.length);
			}
			min = newMin;
		} else if (min + values.length - c == 1) {
			int newLength;
			for (newLength = values.length - 1; newLength > 0; --newLength) {
				if (values[newLength - 1] != null) {
					break;
				}
			}
			values = Arrays.copyOf(values, newLength);
		}
	}

	public void put(char c, V value) {
		if (values == null) {
			min = c;
			values = new Object[1];
			values[0] = value;
			++size;
		} else if (min > c) {
			Object[] newValues = new Object[values.length + min - c];
			for (int i = 0; i < values.length; ++i) {
				newValues[i + min - c] = values[i];
			}
			newValues[0] = value;
			values = newValues;
			min = c;
			++size;
		} else {
			int offset = c - min;
			if (values.length <= offset) {
				values = Arrays.copyOf(values, offset+1);
			}
			if (values[offset] == null) {
				++size;
			}
			values[offset] = value;
		}
	}

	public char nextKey(char c) {
		if (c < min) {
			return min;
		} else {
			for (int i = c  - min + 1; i < values.length; ++i) {
				if (values[i] != null) {
					return (char) (i+min);
				}
			}
		}
		return '\0';
	}

	public int size() {
		return size;
	}

	public static void main(String[] args) {
		FastCharMap<Integer> f = new FastCharMap<Integer>();
		f.put('a', 2);
		System.out.println(f.min);
		System.out.println(f.values.length);
		f.put('c', 4);
		System.out.println(f.min);
		System.out.println(f.values.length);
		f.remove('c');
		System.out.println(f.min);
		System.out.println(f.values.length);
	}
}
