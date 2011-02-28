package net.jpountz.trie;

import junit.framework.TestCase;

public class ImmutableFastArrayTrieTest extends TestCase {

	public void test() {
		FastArrayTrie<Integer> trie = new FastArrayTrie<Integer>();
		trie.put("abef", 3);
		trie.put("abcdef", 1);
		trie.put("z", 6);
		ArrayTrie<Integer> roTrie = trie.immutableCopy();
		assertEquals(3, roTrie.get("abef").intValue());
		assertEquals(1, roTrie.get("abcdef").intValue());
		assertEquals(6, roTrie.get("z").intValue());
	}

}
