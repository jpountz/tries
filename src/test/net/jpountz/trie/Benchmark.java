package net.jpountz.trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {

	protected static final List<TrieFactory> FACTORIES = new ArrayList<TrieFactory>();
	static {
		FACTORIES.add(new TrieFactory() {
			@Override
			public <T> Trie<T> newTrie() {
				return new HashMapTrie<T>();
			}
		});
		FACTORIES.add(new TrieFactory() {
			@Override
			public <T> Trie<T> newTrie() {
				return new SimpleTrie<T>();
			}
		});
		FACTORIES.add(new TrieFactory() {
			@Override
			public <T> Trie<T> newTrie() {
				return new FastCharMapTrie<T>();
			}
		});
		FACTORIES.add(new TrieFactory() {
			@Override
			public <T> Trie<T> newTrie() {
				return new ArrayTrie<T>();
			}
		});
		FACTORIES.add(new TrieFactory() {
			@Override
			public <V> Trie<V> newTrie() {
				return new CompositeTrie<V>(new TrieFactory() {
					@Override
					public <V2> Trie<V2> newTrie() {
						return new FastCharMapTrie<V2>();
					}
				},
				new TrieFactory() {			
					@Override
					public <V2> Trie<V2> newTrie() {
						return new ArrayTrie<V2>();
					}
				}, 2);
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
