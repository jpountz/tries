package net.jpountz.trie;

import java.util.HashMap;
import java.util.Map;

/**
 * Fake trie implementation based on a HashMap for lookup performance comparison.
 */
public class HashMapTrie<T> extends MapTrie<T> {

	@Override
	protected Map<String, T> getMap() {
		return new HashMap<String, T>();
	}

}
