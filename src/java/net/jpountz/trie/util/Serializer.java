package net.jpountz.trie.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An object serializer/deserializer.
 */
public interface Serializer<T> {

	/**
	 * Read the object.
	 *
	 * @param is the data to read
	 * @return the object
	 */
	T read(DataInputStream is) throws IOException;

	/**
	 * Write the object.
	 *
	 * @param value the object
	 * @param os the stream to write to
	 */
	void write(T value, DataOutputStream os) throws IOException;

}
