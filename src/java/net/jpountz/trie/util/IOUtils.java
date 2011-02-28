package net.jpountz.trie.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IOUtils {

	private IOUtils() {}

	public static void writeByte(DataOutputStream os, byte b) throws IOException {
		os.writeByte(b);
	}

	public static void writeInt(DataOutputStream os, int b) throws IOException {
		os.writeInt(b);
	}

	public static void writeVInt(DataOutputStream os, int i) throws IOException {
		while ((i & ~0x7F) != 0) {
			writeByte(os, (byte) ((i & 0x7f) | 0x80));
			i >>>= 7;
		}
		writeByte(os, (byte) i);
	}

	public static void writeLong(DataOutputStream os, long b) throws IOException {
		os.writeLong(b);
	}

	public static void writeVLong(DataOutputStream os, long i) throws IOException {
		while ((i & ~0x7F) != 0) {
			writeByte(os, (byte) ((i & 0x7f) | 0x80));
			i >>>= 7;
		}
		writeByte(os, (byte) i);
	}

	public static byte readByte(DataInputStream is) throws IOException {
		return is.readByte();
	}

	public static int readInt(DataInputStream is) throws IOException {
		return is.readInt();
	}

	public static int readVInt(DataInputStream is) throws IOException {
		byte b = readByte(is);
		int i = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = readByte(is);
			i |= (b & 0x7F) << shift;
		}
		return i;
	}

	public static long readLong(DataInputStream is) throws IOException {
		return is.readLong();
	}

	public long readVLong(DataInputStream is) throws IOException {
		byte b = readByte(is);
		long i = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = readByte(is);
			i |= (b & 0x7FL) << shift;
		}
		return i;
	}

}
