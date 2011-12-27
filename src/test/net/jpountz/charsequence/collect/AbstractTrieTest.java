package net.jpountz.charsequence.collect;

import it.unimi.dsi.fastutil.chars.CharArrayList;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import net.jpountz.charsequence.collect.Trie.Cursor;

public abstract class AbstractTrieTest extends AbstractCharSequenceMapTest {

	protected Trie<Integer> trie;

	public abstract Trie<Integer> newMap();

	@Override
	public void setUp() {
		super.setUp();
		trie = (Trie<Integer>) map;
	}

	public void testRemove() {
		put("abcdef", 3);
		put("abcfgh", 4);
		put("abchij", 5);

		assertEquals(13, trie.nodes());

		trie.remove("abc");
		assertEquals(13, trie.nodes());

		trie.remove("abcdefgh");
		assertEquals(13, trie.nodes());

		trie.remove("abcfgh");
		assertEquals(10, trie.nodes());
		assertEquals(Integer.valueOf(3), trie.get("abcdef"));
		assertEquals(Integer.valueOf(5), trie.get("abchij"));
		assertNull(trie.get("abcfgh"));

		trie.remove("abcdef");
		assertEquals(7, trie.nodes());
		assertEquals(Integer.valueOf(5), trie.get("abchij"));

		trie.remove("abchij");
		assertEquals(1, trie.nodes());
		assertFalse(trie.getCursor().moveToFirstChild());
	}

	public void testEmptyTrie() {
		Cursor<Integer> cursor = trie.getCursor();
		Trie.Node root = cursor.getNode();
		assertFalse(Trie.Traversal.BREADTH_FIRST.moveToNextNode(root, cursor));
		assertFalse(Trie.Traversal.DEPTH_FIRST.moveToNextNode(root, cursor));
		assertFalse(Trie.Traversal.BREADTH_FIRST_THEN_DEPTH.moveToNextNode(root, cursor));
	}

	public void testCursorRW() {
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

	public void testCursorR() {
		put("abc", 2);
		put("accc", 3);
		put("acaa", 4);
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
		put("ab", 1);
		put("abcd", 2);
		put("aed", 4);
		put("aedfg", 5);
		put("aef", 3);
		put("a", 0);
		Cursor<Integer> cursor = trie.getCursor();
		cursor.moveToChild('a');
		Trie.Node under = cursor.getNode();
		Trie.Traversal traversal = Trie.Traversal.DEPTH_FIRST;
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

	public void testMoveToNextNode(Trie.Traversal traversal, String[] labels) {
		put("aef", 3);
		put("ab", 1);
		put("abcd", 2);
		put("aedfg", 4);
		put("aed", 5);
		put("a", 0);
		put("ed", 5);
		put("efg", 8);
		put("eg", 12);
		int n = 0;
		Cursor<Integer> cursor = trie.getCursor();
		for (String label : labels) {
			for (int i = 0; i < label.length(); ++i) {
				assertTrue(cursor.moveToChild(label.charAt(i)));
			}
			cursor.reset();
		}
		Trie.Node root = cursor.getNode();
		while (traversal.moveToNextNode(root, cursor)) {
			assertEquals(labels[n++], cursor.getLabel());
		}
		++n;
		assertEquals(n, trie.nodes());
	}

	public void testMoveToNextNodeDF() {
		String[] labels = new String[] {
				"a", "ab", "abc", "abcd", "ae", "aed", "aedf", "aedfg", "aef", "e", "ed", "ef", "efg", "eg"
		};
		testMoveToNextNode(Trie.Traversal.DEPTH_FIRST, labels);
	}

	public void testMoveToNextNodeBFTD() {
		String[] labels = new String[] {
				"a", "e", "ab", "ae", "abc", "abcd", "aed", "aef", "aedf", "aedfg", "ed", "ef", "eg", "efg"
		};
		testMoveToNextNode(Trie.Traversal.BREADTH_FIRST_THEN_DEPTH, labels);
	}

	public void testMoveToNextNodeBF() {
		String[] labels = new String[] {
				"a", "e", "ab", "ae", "ed", "ef", "eg", "abc", "aed", "aef", "efg", "abcd", "aedf", "aedfg"
		};
		testMoveToNextNode(Trie.Traversal.BREADTH_FIRST, labels);
	}

	public void testGetNeightbors() {
		put("aabc", 1);
		put("acd", 2);
		put("zabc", 3);
		put("abcde", 4);
		put("abcdefg", 10);
		
		Comparator<Map.Entry<String, Integer>> comparator = new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		};
		
		SortedSet<Map.Entry<String, Integer>> neighbors = new TreeSet<Map.Entry<String, Integer>>(comparator);
		Tries.getNeighbors("abc", trie, 2, neighbors);
		assertEquals(4, neighbors.size());
	}

}
