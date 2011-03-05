package net.jpountz.trie;

import java.util.Map;
import java.util.TreeMap;

public class TreeMapTrie<T> extends MapTrie<T> {

	@Override
	protected Map<String, T> getMap() {
		return new TreeMap<String, T>();
	}

}
