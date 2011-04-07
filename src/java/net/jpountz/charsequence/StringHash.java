package net.jpountz.charsequence;

/**
 * Hash implementation as described in the {@link String#hashCode()}
 * documentation.
 */
public class StringHash implements Hash {

	public static final StringHash INSTANCE = new StringHash();

	private StringHash() {} // no instantiation

	@Override
	public int hash(char[] buffer, int offset, int length) {
		final int prime = 31;
		int hash = 0;
		int o = offset;
		for (int i = 0; i < length; ++i) {
			hash = prime * hash + buffer[o++];
		}
		return hash;
	}

	@Override
	public int hash(CharSequence sequence, int offset, int length) {
		if (sequence instanceof String && offset == 0 && length == sequence.length()) {
			// because string hashs are cached
			return ((String) sequence).hashCode();
		}
		final int prime = 31;
		int hash = 0;
		int o = offset;
		for (int i = 0; i < length; ++i) {
			hash = prime * hash + sequence.charAt(o++);
		}
		return hash;
	}

}
