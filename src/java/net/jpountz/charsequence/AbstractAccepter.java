package net.jpountz.charsequence;

/**
 * Base implementation for an {@link Accepter}.
 */
public abstract class AbstractAccepter implements Accepter {

	@Override
	public boolean accept(CharSequence seq) {
		return accept(seq, 0, seq.length());
	}

	public boolean accept(char[] seq, int off, int len) {
		return accept(new String(seq, off, len), 0, len);
	}

	@Override
	public boolean accept(char[] seq) {
		return accept(seq, 0, seq.length);
	}

}
