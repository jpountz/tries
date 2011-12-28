package net.jpountz.charsequence;

import java.util.Comparator;


/**
 * Comparator of characters.
 */
public abstract class CharComparator {

	/**
	 * Default {@link CharComparator}. Returns:
	 *   compare(c1, c2) == 0 only if, and only if c1 == c2,
	 *   compare(c1, c2) < 0 if, and only if c1 < c2.
	 */
	public static final CharComparator DEFAULT = new CharComparator() {

		public int compare(char c1, char c2) {
			return (int) c1 - c2;
		}

	};

	public abstract int compare(char c1, char c2);

	public final int compare(CharSequence key1, int off1, int len1, CharSequence key2, int off2, int len2) {
		int len = Math.min(len1, len2);
		for (int i = 0; i < len; ++i) {
			int cmp = compare(key1.charAt(off1 + i), key2.charAt(off2 + i));
			if (cmp != 0) {
				return cmp;
			}
		}
		return len1 - len2;
	}

	public final int compare(CharSequence key1, CharSequence key2) {
		return compare(key1, 0, key1.length(), key2, 0, key2.length());
	}

	public final int compare(char[] key1, int off1, int len1, char[] key2, int off2, int len2) {
		int len = Math.min(len1, len2);
		for (int i = 0; i < len; ++i) {
			int cmp = compare(key1[off1 + i], key2[off2 + i]);
			if (cmp != 0) {
				return cmp;
			}
		}
		return len1 - len2;
	}

	public final int compare(char[] key1, char[] key2) {
		return compare(key1, 0, key1.length, key2, 0, key2.length);
	}

	public final int compare(CharSequence key1, int off1, int len1, char[] key2, int off2, int len2) {
		int len = Math.min(len1, len2);
		for (int i = 0; i < len; ++i) {
			int cmp = compare(key1.charAt(off1 + i), key2[off2 + i]);
			if (cmp != 0) {
				return cmp;
			}
		}
		return len1 - len2;
	}

	public final int compare(CharSequence key1, char[] key2) {
		return compare(key1, 0, key1.length(), key2, 0, key2.length);
	}

	public final int compare(char[] key1, int off1, int len1, CharSequence key2, int off2, int len2) {
		return - compare(key2, off2, len2, key1, off1, len1);
	}

	public final int compare(char[] key1, CharSequence key2) {
		return compare(key1, 0, key1.length, key2, 0, key2.length());
	}

	public final <T extends CharSequence> Comparator<T> asCharSequenceComparator() {
		return new Comparator<T>() {

			@Override
			public int compare(T arg0, T arg1) {
				return CharComparator.this.compare(arg0, arg1);
			}

		};
	}

	public final Comparator<char[]> asCharArrayComparator() {
		return new Comparator<char[]>() {

			@Override
			public int compare(char[] arg0, char[] arg1) {
				return CharComparator.this.compare(arg0, arg1);
			}

		};
	}

}
