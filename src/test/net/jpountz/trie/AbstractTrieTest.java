package net.jpountz.trie;

import junit.framework.TestCase;


public abstract class AbstractTrieTest extends TestCase {

	private Trie<Integer> trie;

	public abstract Trie<Integer> newTrie();

	public void setUp() {
		trie = newTrie();
	}

	public void test1() {
		trie.put("ab", 1);
		trie.put("abcd", 2);
		assertEquals(1, trie.get("ab").intValue());
		assertEquals(2, trie.get("abcd").intValue());
	}

	public void test2() {
		trie.put("abcd", 2);
		trie.put("ab", 1);
		assertEquals(1, trie.get("ab").intValue());
		assertEquals(2, trie.get("abcd").intValue());
	}

	public void test3() {
		trie.put("abcd", 1);
		trie.put("abef", 2);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
	}

	public void test4() {
		trie.put("abef", 2);
		trie.put("abcd", 1);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
	}

	public void test5() {
		trie.put("abcd", 1);
		trie.put("abef", 2);
		trie.put("abefgh", 3);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
		assertEquals(3, trie.get("abefgh").intValue());
	}

	public void test6() {
		trie.put("abcd", 1);
		trie.put("abefgh", 3);
		trie.put("abef", 2);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
		assertEquals(3, trie.get("abefgh").intValue());
	}

	public void test7() {
		trie.put("abcd", 1);
		trie.put("abgh", 3);
		trie.put("abef", 2);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
		assertEquals(3, trie.get("abgh").intValue());
	}

	public void test8() {
		trie.put("abcdef", 1);
		trie.put("abcdfg", 2);
		trie.put("abij", 3);
		assertEquals(1, trie.get("abcdef").intValue());
		assertEquals(2, trie.get("abcdfg").intValue());
		assertEquals(3, trie.get("abij").intValue());
	}

	public void test9() {
		trie.put("abcd", 1);
		trie.put("abef", 2);
		trie.put("abab", 3);
		trie.put("ABCD", 4);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
		assertEquals(3, trie.get("abab").intValue());
		assertEquals(4, trie.get("ABCD").intValue());
	}

	public void test10() {
		trie.put("ab", 1);
		trie.put("abcd", 2);
		trie.put("a", 0);
		assertEquals(0, trie.get("a").intValue());
		assertEquals(1, trie.get("ab").intValue());
		assertEquals(2, trie.get("abcd").intValue());
	}
}
