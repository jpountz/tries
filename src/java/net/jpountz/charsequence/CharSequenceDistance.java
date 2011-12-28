package net.jpountz.charsequence;

import java.nio.CharBuffer;

/**
 * Distance between two {@link CharSequence}s.
 */
public abstract class CharSequenceDistance {

	/**
	 * Distance from 'from' to 'to'. Although it is not necessary that this
	 * distance be symetric, it may be required by some methods.
	 *
	 * @param from the source {@link CharSequence}
	 * @param off1
	 * @param len1
	 * @param to the target {@link CharSequence}
	 * @param off2
	 * @param len2
	 * @return the distance
	 */
	public abstract double distance(CharSequence from, int off1, int len1, CharSequence to, int off2, int len2);

	public final double distance(CharSequence from, CharSequence to) {
		return distance(from, 0, from.length(), to, 0, to.length());
	}

	public double distance(char[] from, int off1, int len1, char[] to, int off2, int len2) {
		return distance(CharBuffer.wrap(from), off1, len1, CharBuffer.wrap(to), off2, len2);
	}

	public final double distance(char[] from, char[] to) {
		return distance(from, 0, from.length, to, 0, to.length);
	}

	public double distance(CharSequence from, int off1, int len1, char[] to, int off2, int len2) {
		return distance(from, off1, len1, CharBuffer.wrap(to), off2, len2);
	}

	public final double distance(CharSequence from, char[] to) {
		return distance(from, 0, from.length(), to, 0, to.length);
	}

	public double distance(char[] from, int off1, int len1, CharSequence to, int off2, int len2) {
		return distance(CharBuffer.wrap(from), off1, len1, to, off2, len2);
	}

	public final double distance(char[] from, CharSequence to) {
		return distance(from, 0, from.length, to, 0, to.length());
	}

}
