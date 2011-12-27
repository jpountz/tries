package net.jpountz.charsequence;

public enum CharSequenceUtils {
	;

	public static char[] toChars(CharSequence seq, int offset, int length) {
		int len = Math.min(seq.length() - offset, length);
		char[] result = new char[len];
		if (seq instanceof String) {
			((String) seq).getChars(offset, offset+len, result, 0);
		} else {
			for (int i = 0; i < length; ++i) {
				result[i] = seq.charAt(offset+i);
			}
		}
		return result;
	}

	public static boolean equals(char[] seq1, int off1, int len1, CharSequence seq2, int off2, int len2) {
		if (len1 == len2) {
			for (int i = 0; i < len1; ++i) {
				if (seq1[off1 + i] != seq2.charAt(off2 + i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
