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
				return new TreeMapTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new CompactArrayTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new BloomFilteredTrie<Boolean>(
						new TrieFactory<Boolean>() {
							@Override
							public Trie<Boolean> newTrie() {
								return new CompactArrayTrie<Boolean>();
							}
						}, StringHash.INSTANCE, 1024*64);
			}
		});
		/*FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new IntermediateArrayTrie<Boolean>();
			}
		});*/
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new FastArrayTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new CompositeTrie<Boolean>(new TrieFactory<Object>() {
					@Override
					public Trie<Object> newTrie() {
						return new FastArrayTrie<Object>();
					}
				},
				new TrieFactory<Boolean>() {
					@Override
					public Trie<Boolean> newTrie() {
						return new CompactArrayTrie<Boolean>();
					}
				}, 2);
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new CompactArrayRadixTrie<Boolean>();
			}
		});
	}

	protected static List<String> readWords(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		List<String> words = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			words.add(line);
		}
		return words;
	}

}
