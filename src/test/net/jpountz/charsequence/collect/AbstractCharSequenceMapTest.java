package net.jpountz.charsequence.collect;

import java.util.Iterator;
import java.util.Map.Entry;

import junit.framework.TestCase;


public abstract class AbstractCharSequenceMapTest extends TestCase {

	protected CharSequenceMap<Integer> map;

	public abstract CharSequenceMap<Integer> newMap();

	public void setUp() {
		map = newMap();
	}

	public void test1() {
		map.put("ab", 1);
		map.put("abcd", 2);
		assertEquals(1, map.get("ab").intValue());
		assertEquals(2, map.get("abcd").intValue());
	}

	public void test2() {
		map.put("abcd", 2);
		map.put("ab", 1);
		assertEquals(1, map.get("ab").intValue());
		assertEquals(2, map.get("abcd").intValue());
	}

	public void test3() {
		map.put("abcd", 1);
		map.put("abef", 2);
		assertEquals(1, map.get("abcd").intValue());
		assertEquals(2, map.get("abef").intValue());
	}

	public void test4() {
		map.put("abef", 2);
		map.put("abcd", 1);
		assertEquals(1, map.get("abcd").intValue());
		assertEquals(2, map.get("abef").intValue());
	}

	public void test5() {
		map.put("abcd", 1);
		map.put("abef", 2);
		map.put("abefgh", 3);
		assertEquals(1, map.get("abcd").intValue());
		assertEquals(2, map.get("abef").intValue());
		assertEquals(3, map.get("abefgh").intValue());
	}

	public void test6() {
		map.put("abcd", 1);
		map.put("abefgh", 3);
		map.put("abef", 2);
		assertEquals(1, map.get("abcd").intValue());
		assertEquals(2, map.get("abef").intValue());
		assertEquals(3, map.get("abefgh").intValue());
	}

	public void test7() {
		map.put("abcd", 1);
		map.put("abgh", 3);
		map.put("abef", 2);
		assertEquals(1, map.get("abcd").intValue());
		assertEquals(2, map.get("abef").intValue());
		assertEquals(3, map.get("abgh").intValue());
	}

	public void test8() {
		map.put("abcdef", 1);
		map.put("abcdfg", 2);
		map.put("abij", 3);
		assertEquals(1, map.get("abcdef").intValue());
		assertEquals(2, map.get("abcdfg").intValue());
		assertEquals(3, map.get("abij").intValue());
	}

	public void test9() {
		map.put("abcd", 1);
		map.put("abef", 2);
		map.put("abab", 3);
		//trie.put("ABCD", 4);
		assertEquals(1, map.get("abcd").intValue());
		assertEquals(2, map.get("abef").intValue());
		assertEquals(3, map.get("abab").intValue());
		//assertEquals(4, trie.get("ABCD").intValue());
	}

	public void test10() {
		map.put("ab", 1);
		map.put("abcd", 2);
		map.put("a", 0);
		assertEquals(0, map.get("a").intValue());
		assertEquals(1, map.get("ab").intValue());
		assertEquals(2, map.get("abcd").intValue());
	}

	public void testEntrySet1() {
		assertFalse(map.entrySet().iterator().hasNext());
	}

	public void testEntrySet2() {
		map.put("abc", 1);
		Iterator<Entry<String, Integer>> it = map.entrySet().iterator();
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		Entry<String, Integer> next = it.next();
		assertEquals("abc", next.getKey());
		assertEquals(Integer.valueOf(1), next.getValue());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}

	public void testEntrySet3() {
		map.put("abc", 1);
		assertEquals("abc", map.entrySet().iterator().next().getKey());
	}

}
