package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import junit.framework.TestCase;
import net.jpountz.trie.Trie.Cursor;


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

	public void testCursor() {
		Cursor<Integer> cursor = trie.getCursor();
		assertFalse(cursor.moveToParent());
		CharArrayList children = new CharArrayList();
		cursor.getChildrenLabels(children);
		assertEquals(0, children.size());
		cursor.addChild('a');
		assertEquals('a', cursor.getEdgeLabel());
		assertEquals("a", cursor.getLabel());
		cursor.getChildrenLabels(children);
		assertEquals(0, children.size());
		assertTrue(cursor.moveToParent());
		assertEquals('\0', cursor.getEdgeLabel());
		assertEquals("", cursor.getLabel());
		cursor.getChildrenLabels(children);
		assertEquals(1, children.size());
		cursor.moveToChild('a');
		assertEquals('a', cursor.getEdgeLabel());
		assertEquals("a", cursor.getLabel());
		cursor.reset();
		assertEquals('\0', cursor.getEdgeLabel());
		assertEquals("", cursor.getLabel());
		assertEquals(1, children.size());
	}

	public void testMoveToNextSuffix() {
		trie.put("ab", 1);
		trie.put("abcd", 2);
		trie.put("aed", 4);
		trie.put("aedfg", 5);
		trie.put("aef", 3);
		trie.put("a", 0);
		Cursor<Integer> cursor = trie.getCursor();
		cursor.moveToChild('a');
		Trie.Node under = cursor.getNode();
		assertTrue(cursor.moveToNextSuffix(under));
		assertEquals("ab", cursor.getLabel());
		assertTrue(cursor.moveToNextSuffix(under));
		assertEquals("abcd", cursor.getLabel());
		assertTrue(cursor.moveToNextSuffix(under));
		assertEquals("aed", cursor.getLabel());
		assertTrue(cursor.moveToNextSuffix(under));
		assertEquals("aedfg", cursor.getLabel());
		assertTrue(cursor.moveToNextSuffix(under));
		assertEquals("aef", cursor.getLabel());
		assertFalse(cursor.moveToNextSuffix(under));
	}
}
