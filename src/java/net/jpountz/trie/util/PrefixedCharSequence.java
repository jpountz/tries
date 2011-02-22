package net.jpountz.trie.util;

/**
 * Immutable implementation of CharSequence which consists of a
 * {@link CharSequence} and one char appended at the end of it.
 */
public class PrefixedCharSequence implements CharSequence {

	private final CharSequence prefix;
	private final char lastChar;

	public PrefixedCharSequence (CharSequence prefix, char lastChar) {
		if (prefix == null) {
			throw new IllegalArgumentException("prefix must not be null");
		}
		this.prefix = prefix;
		this.lastChar = lastChar;
	}

	@Override
	public char charAt(int index) {
		if (index < prefix.length()) {
			return prefix.charAt(index);
		} else if (index == prefix.length()) {
			return lastChar;
		} else {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	@Override
	public int length() {
		return prefix.length() + 1;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (end <= prefix.length()) {
			return prefix.subSequence(start, end);
		} else if (end == length()) {
			if (start == prefix.length()) {
				return Character.toString(lastChar);
			} else {
				return new PrefixedCharSequence(prefix.subSequence(start, end-1), lastChar);
			}
		} else {
			throw new ArrayIndexOutOfBoundsException(end);
		}
	}

	@Override
	public String toString() {
		return new StringBuilder(prefix).append(lastChar).toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lastChar;
		result = prime * result + prefix.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrefixedCharSequence other = (PrefixedCharSequence) obj;
		if (lastChar != other.lastChar)
			return false;
		if (!prefix.equals(other.prefix))
			return false;
		return true;
	}

}
