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

	protected void put(String key, Integer value) {
		map.put(key, value);
	}

	public void test1() {
		put("ab", 1);
		put("abcd", 2);
		assertEquals(Integer.valueOf(1), map.get("ab"));
		assertEquals(Integer.valueOf(2), map.get("abcd"));
	}

	public void test2() {
		put("abcd", 2);
		put("ab", 1);
		assertEquals(Integer.valueOf(1), map.get("ab"));
		assertEquals(Integer.valueOf(2), map.get("abcd"));
	}

	public void test3() {
		put("abcd", 1);
		put("abef", 2);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
	}

	public void test4() {
		put("abef", 2);
		put("abcd", 1);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
	}

	public void test5() {
		put("abcd", 1);
		put("abef", 2);
		put("abefgh", 3);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abefgh"));
	}

	public void test6() {
		put("abcd", 1);
		put("abefgh", 3);
		put("abef", 2);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abefgh"));
	}

	public void test7() {
		put("abcd", 1);
		put("abgh", 3);
		put("abef", 2);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abgh"));
	}

	public void test8() {
		put("abcdef", 1);
		put("abcdfg", 2);
		put("abij", 3);
		assertEquals(Integer.valueOf(1), map.get("abcdef"));
		assertEquals(Integer.valueOf(2), map.get("abcdfg"));
		assertEquals(Integer.valueOf(3), map.get("abij"));
	}

	public void test9() {
		put("abcd", 1);
		put("abef", 2);
		put("abab", 3);
		put("ABCD", 4);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abab"));
		assertEquals(Integer.valueOf(4), map.get("ABCD"));
	}

	public void test10() {
		put("ab", 1);
		put("abcd", 2);
		put("a", 0);
		assertEquals(Integer.valueOf(0), map.get("a"));
		assertEquals(Integer.valueOf(1), map.get("ab"));
		assertEquals(Integer.valueOf(2), map.get("abcd"));
	}

	public void testEntrySet1() {
		assertFalse(map.entrySet().iterator().hasNext());
	}

	public void testEntrySet2() {
		put("abc", 1);
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
		put("abc", 1);
		assertEquals("abc", map.entrySet().iterator().next().getKey());
	}

}
