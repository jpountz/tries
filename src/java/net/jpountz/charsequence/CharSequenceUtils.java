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

}
