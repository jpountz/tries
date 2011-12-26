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
		assertEquals(Integer.valueOf(1), map.get("ab"));
		assertEquals(Integer.valueOf(2), map.get("abcd"));
	}

	public void test2() {
		map.put("abcd", 2);
		map.put("ab", 1);
		assertEquals(Integer.valueOf(1), map.get("ab"));
		assertEquals(Integer.valueOf(2), map.get("abcd"));
	}

	public void test3() {
		map.put("abcd", 1);
		map.put("abef", 2);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
	}

	public void test4() {
		map.put("abef", 2);
		map.put("abcd", 1);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
	}

	public void test5() {
		map.put("abcd", 1);
		map.put("abef", 2);
		map.put("abefgh", 3);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abefgh"));
	}

	public void test6() {
		map.put("abcd", 1);
		map.put("abefgh", 3);
		map.put("abef", 2);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abefgh"));
	}

	public void test7() {
		map.put("abcd", 1);
		map.put("abgh", 3);
		map.put("abef", 2);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abgh"));
	}

	public void test8() {
		map.put("abcdef", 1);
		map.put("abcdfg", 2);
		map.put("abij", 3);
		assertEquals(Integer.valueOf(1), map.get("abcdef"));
		assertEquals(Integer.valueOf(2), map.get("abcdfg"));
		assertEquals(Integer.valueOf(3), map.get("abij"));
	}

	public void test9() {
		map.put("abcd", 1);
		map.put("abef", 2);
		map.put("abab", 3);
		map.put("ABCD", 4);
		assertEquals(Integer.valueOf(1), map.get("abcd"));
		assertEquals(Integer.valueOf(2), map.get("abef"));
		assertEquals(Integer.valueOf(3), map.get("abab"));
		assertEquals(Integer.valueOf(4), map.get("ABCD"));
	}

	public void test10() {
		map.put("ab", 1);
		map.put("abcd", 2);
		map.put("a", 0);
		assertEquals(Integer.valueOf(0), map.get("a"));
		assertEquals(Integer.valueOf(1), map.get("ab"));
		assertEquals(Integer.valueOf(2), map.get("abcd"));
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
