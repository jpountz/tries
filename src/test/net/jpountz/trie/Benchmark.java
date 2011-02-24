package net.jpountz.trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {

	protected static final List<TrieFactory<Boolean>> FACTORIES = new ArrayList<TrieFactory<Boolean>>();
	static {
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new HashMapTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new SimpleTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new FastCharMapTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new ArrayTrie<Boolean>();
			}
		});
	}

	protected static List<char[]> readWords(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		List<char[]> words = new ArrayList<char[]>();
		String line;
		while ((line = reader.readLine()) != null) {
			char[] buffer = new char[line.length()];
			line.getChars(0, line.length(), buffer, 0);
			words.add(buffer);
		}
		return words;
	}

}
