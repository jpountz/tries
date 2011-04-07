package net.jpountz.charsequence;

/**
 * Compute the hash of a char sequence.
 */
public interface Hash {

	int hash(char[] buffer, int offset, int length);

	int hash(CharSequence sequence, int offset, int length);

}
