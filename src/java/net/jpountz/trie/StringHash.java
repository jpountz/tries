package net.jpountz.trie;

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
		for (int i = 0; i < length; ++i) {
			hash = prime * hash + buffer[offset+i];
		}
		return hash;
	}

	@Override
	public int hash(CharSequence sequence, int offset, int length) {
		final int prime = 31;
		int hash = 0;
		for (int i = 0; i < length; ++i) {
			hash = prime * hash + sequence.charAt(offset+i);
		}
		return hash;
	}

}
