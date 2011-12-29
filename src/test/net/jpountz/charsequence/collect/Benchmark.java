package net.jpountz.charsequence.collect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Benchmark {

	public enum CharSequenceMapFactory {
		HASH_MAP {
			@Override
			Map<String, String> newMap() {
				return new HashMap<String, String>();
			}
		},
		TREE_MAP {
			@Override
			Map<String, String> newMap() {
				return new TreeMap<String, String>();
			}
		},
		CHAR_ARRAY_HASH_MAP {
			@Override
			Map<String, String> newMap() {
				return new CharArrayHashMap<String>();
			}			
		},
		LIST_TRIE {
			@Override
			Map<String, String> newMap() {
				return new ListTrie<String>();
			}
		},
		LIST_RADIX_TRIE {
			@Override
			Map<String, String> newMap() {
				return new ListRadixTrie<String>();
			}
		},
		COMPOSITE_TRIE {

			@Override
			Map<String, String> newMap() {
				return new CompositeTrie<String>(
						new TrieFactory<Object>() {
							@Override
							public Trie<Object> newTrie() {
								return new ArrayTrie<Object>();
							}
						},
						new TrieFactory<String>() {
							@Override
							public Trie<String> newTrie() {
								return new ListTrie<String>();
							}
						}, 2);
			}
			
		},
		ARRAY_TRIE {
			@Override
			Map<String, String> newMap() {
				return new ArrayTrie<String>();
			}
		},
		BINARY_SEARCH_TRIE {
			@Override
			Map<String, String> newMap() {
				return Tries.sortedCharSequenceListAsTrie(Collections.<String>emptyList(), Collections.<String>emptyList());
			}
		};
		abstract Map<String, String> newMap();
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
