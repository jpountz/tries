package net.jpountz.trie;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.jpountz.trie.util.IOUtils;
import net.jpountz.trie.util.Serializer;

public class Main extends Benchmark {

	static Serializer<Integer> intSerializer = new Serializer<Integer>() {

		@Override
		public Integer read(DataInputStream is) throws IOException {
			int i = IOUtils.readVInt(is);
			if ((i & 1) > 0) {
				return Integer.valueOf(i >> 1);
			} else {
				return null;
			}
		}

		@Override
		public void write(Integer value, DataOutputStream os) throws IOException {
			if (value == null) {
				IOUtils.writeVInt(os, 0);
			} else {
				int v = value.intValue();
				v = (v << 1) | 1;
				IOUtils.writeVInt(os, v);
			}
		}
		
	};

}
