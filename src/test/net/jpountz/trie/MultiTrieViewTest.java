package net.jpountz.trie;

import net.jpountz.trie.Trie.Cursor;
import net.jpountz.trie.Trie.Node;
import junit.framework.TestCase;

public class MultiTrieViewTest extends TestCase {

	private Trie<Integer> t1;
	private Trie<Integer> t2;
	private Trie<Integer> trie;

	@Override
	protected void setUp() throws Exception {
		t1 = new CompactArrayTrie<Integer>();
		t2 = new CompactArrayTrie<Integer>();
		trie = new MultiTrieView<Integer>(t1, t2);
	}

	public void test1() {
		t2.put("", 0);
		t2.put("aba", 1);
		t1.put("abc", 2);
		t1.put("ad", 3);
		t2.put("z", 4);
		Cursor<Integer> cursor = trie.getCursor();
		Node node = cursor.getNode();
		do {
			//System.out.println(cursor.getLabel() + " --> " + cursor.getValue());
		} while (TrieTraversal.DEPTH_FIRST.moveToNextNode(node, cursor));
	}

	public void test2() {
		t1.put("", 0);
		t1.put("trie", 1);
		t2.put("tas", 3);
		t2.put("test", 4);
		Cursor<Integer> cursor = trie.getCursor();
		Node node = cursor.getNode();
		do {
			System.out.println(cursor.getLabel() + " --> " + cursor.getValue());
		} while (TrieTraversal.BREADTH_FIRST_THEN_DEPTH.moveToNextNode(node, cursor));
	}

}
