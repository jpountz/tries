package net.jpountz.charsequence;

/**
 * An accepter accepts strings.
 */
public interface Accepter {

	boolean accept(CharSequence seq);
	boolean accept(CharSequence seq, int offset, int length);
	boolean accept(char[] seq);
	boolean accept(char[] seq, int offset, int length);

}
