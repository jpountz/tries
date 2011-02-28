package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.SortedSet;
import java.util.TreeSet;

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
		//trie.put("ABCD", 4);
		assertEquals(1, trie.get("abcd").intValue());
		assertEquals(2, trie.get("abef").intValue());
		assertEquals(3, trie.get("abab").intValue());
		//assertEquals(4, trie.get("ABCD").intValue());
	}

	public void test10() {
		trie.put("ab", 1);
		trie.put("abcd", 2);
		trie.put("a", 0);
		assertEquals(0, trie.get("a").intValue());
		assertEquals(1, trie.get("ab").intValue());
		assertEquals(2, trie.get("abcd").intValue());
	}

	public void testEmptyTrie() {
		Cursor<Integer> cursor = trie.getCursor();
		Trie.Node root = cursor.getNode();
		assertFalse(TrieTraversal.BREADTH_FIRST.moveToNextNode(root, cursor));
		assertFalse(TrieTraversal.DEPTH_FIRST.moveToNextNode(root, cursor));
		assertFalse(TrieTraversal.BREADTH_FIRST_THEN_DEPTH.moveToNextNode(root, cursor));
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

	public void testCursor2() {
		trie.put("abc", 2);
		trie.put("accc", 3);
		trie.put("acaa", 4);
		Trie.Cursor<Integer> cursor = trie.getCursor();
		assertFalse(cursor.moveToBrother());
		assertTrue(cursor.moveToFirstChild());
		assertEquals("a", cursor.getLabel());
		assertFalse(cursor.moveToBrother());
		assertTrue(cursor.moveToFirstChild());
		assertEquals("ab", cursor.getLabel());
		assertTrue(cursor.moveToBrother());
		assertEquals("ac", cursor.getLabel());
		assertFalse(cursor.moveToBrother());
		assertTrue(cursor.moveToFirstChild());
		assertTrue(cursor.moveToFirstChild());
		assertEquals("acaa", cursor.getLabel());
		assertFalse(cursor.moveToBrother());
		assertNotNull(cursor.getValue());
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
		TrieTraversal traversal = TrieTraversal.DEPTH_FIRST;
		assertTrue(Tries.moveToNextSuffix(under, cursor, traversal));
		assertEquals("ab", cursor.getLabel());
		assertTrue(Tries.moveToNextSuffix(under, cursor, traversal));
		assertEquals("abcd", cursor.getLabel());
		assertTrue(Tries.moveToNextSuffix(under, cursor, traversal));
		assertEquals("aed", cursor.getLabel());
		assertTrue(Tries.moveToNextSuffix(under, cursor, traversal));
		assertEquals("aedfg", cursor.getLabel());
		assertTrue(Tries.moveToNextSuffix(under, cursor, traversal));
		assertEquals("aef", cursor.getLabel());
		assertFalse(Tries.moveToNextSuffix(under, cursor, traversal));
	}

	public void testMoveToNextNode(TrieTraversal traversal, String[] labels) {
		trie.put("aef", 3);
		trie.put("ab", 1);
		trie.put("abcd", 2);
		trie.put("aedfg", 4);
		trie.put("aed", 5);
		trie.put("a", 0);
		trie.put("ed", 5);
		trie.put("efg", 8);
		trie.put("eg", 12);
		int n = 0;
		Cursor<Integer> cursor = trie.getCursor();
		Trie.Node root = cursor.getNode();
		while (traversal.moveToNextNode(root, cursor)) {
			assertEquals(labels[n++], cursor.getLabel());
		}
		++n;
		assertEquals(n, trie.size());
	}

	public void testMoveToNextNodeDF() {
		String[] labels = new String[] {
				"a", "ab", "abc", "abcd", "ae", "aed", "aedf", "aedfg", "aef", "e", "ed", "ef", "efg", "eg"
		};
		testMoveToNextNode(TrieTraversal.DEPTH_FIRST, labels);
	}

	public void testMoveToNextNodeBFTD() {
		String[] labels = new String[] {
				"a", "e", "ab", "ae", "abc", "abcd", "aed", "aef", "aedf", "aedfg", "ed", "ef", "eg", "efg"
		};
		testMoveToNextNode(TrieTraversal.BREADTH_FIRST_THEN_DEPTH, labels);
	}

	public void testMoveToNextNodeBF() {
		String[] labels = new String[] {
				"a", "e", "ab", "ae", "ed", "ef", "eg", "abc", "aed", "aef", "efg", "abcd", "aedf", "aedfg"
		};
		testMoveToNextNode(TrieTraversal.BREADTH_FIRST, labels);
	}

	public void testGetNeightbors() {
		trie.put("aabc", 1);
		trie.put("acd", 2);
		trie.put("zabc", 3);
		trie.put("abcde", 4);
		trie.put("abcdefg", 10);
		
		SortedSet<Trie.Entry<Integer>> neighbors = new TreeSet<Trie.Entry<Integer>>();
		Tries.getNeighbors("abc", trie, 2, neighbors);
		assertEquals(4, neighbors.size());
	}
}
