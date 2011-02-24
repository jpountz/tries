package net.jpountz.trie;

import java.io.IOException;
import java.util.List;

public class Main extends Benchmark {

	public static void main(String[] args) throws IOException {
		List<char[]> words = readWords("/tmp/en.txt");
		ArrayTrie<Boolean> trie = new ArrayTrie<Boolean>();
		for (char[] word : words) {
			trie.put(word, Boolean.TRUE);
		}
		
	}

}
