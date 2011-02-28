package net.jpountz.trie.util;

import java.io.ByteArrayInputStream;

public class MutableByteArrayInputStream extends ByteArrayInputStream {

	public MutableByteArrayInputStream(byte[] buf, int offset, int length) {
		super(buf, offset, length);
	}

	public MutableByteArrayInputStream(byte[] buf) {
		this(buf, 0, buf.length);
	}

	public byte[] getWrapped() {
		return buf;
	}

	public void setWrapped(byte[] buf, int offset, int length) {
		this.buf = buf;
		this.mark = offset;
		this.count = length;
	}

}
