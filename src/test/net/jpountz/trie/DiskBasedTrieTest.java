package net.jpountz.trie;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import net.jpountz.trie.DiskBasedTrie.DiskBasedCursor;
import net.jpountz.trie.Trie.Node;
import net.jpountz.trie.util.IOUtils;
import net.jpountz.trie.util.Serializer;

public class DiskBasedTrieTest extends TestCase {

	public static final Serializer<Boolean> BOOLEAN_SERIALIZER = new Serializer<Boolean>() {

		static final byte NULL  = 2;
		static final byte FALSE = 0;
		static final byte TRUE  = 1;

		@Override
		public Boolean read(DataInputStream is) throws IOException {
			switch (IOUtils.readByte(is)) {
			case NULL:
				return null;
			case FALSE:
				return Boolean.FALSE;
			case TRUE:
				return Boolean.TRUE;
			default:
				throw new IOException("Invalid byte read");
			}
		}

		@Override
		public void write(Boolean value, DataOutputStream os)
				throws IOException {
			if (value == null) {
				IOUtils.writeByte(os, NULL);
			} else if (value.booleanValue()) {
				IOUtils.writeByte(os, TRUE);
			} else {
				IOUtils.writeByte(os, FALSE);
			}
		}

	};

	private File dir;
	private DiskBasedTrie<Boolean> trie;

	public void setUp() throws IOException {
		dir = File.createTempFile("trie-test", null);
		dir.delete();
		trie = new DiskBasedTrie<Boolean>(dir, BOOLEAN_SERIALIZER, TrieTraversal.DEPTH_FIRST);
	}

	public void tearDown() {
		for (File file : dir.listFiles()) {
			file.delete();
		}
		dir.delete();
	}

	public void testUncommitted() {
		trie.put("test", Boolean.FALSE);
		trie.put("test2", Boolean.TRUE);
		assertEquals(1, trie.size()); // the root node
	}

	public void testCommit() throws IOException {
		trie.put("test", Boolean.TRUE);
		trie.put("tas", Boolean.TRUE);
		trie.put("", Boolean.TRUE);
		trie.commit();
		assertTrue(trie.get("test"));
		assertTrue(trie.get("tas"));
		assertTrue(trie.get(""));
	}

	public void testCommitTwice() throws IOException {
		trie.put("test", Boolean.TRUE);
		trie.put("tas", Boolean.TRUE);
		trie.commit();
		trie.put("", Boolean.TRUE);
		trie.put("trie", Boolean.TRUE);
		trie.commit();
		assertTrue(trie.get("test"));
		assertTrue(trie.get("tas"));
		assertTrue(trie.get(""));
		assertTrue(trie.get("test"));
		assertTrue(trie.get("tas"));
		assertTrue(trie.get("trie"));
	}

	public void testReopen() throws IOException {
		trie.put("test", Boolean.TRUE);
		trie.put("tas", Boolean.TRUE);
		trie.put("", Boolean.TRUE);
		trie.commit();
		DiskBasedTrie<Boolean> trie2 = new DiskBasedTrie<Boolean>(dir, BOOLEAN_SERIALIZER, TrieTraversal.DEPTH_FIRST);
		DiskBasedCursor<Boolean> c1 = trie.getCursor();
		DiskBasedCursor<Boolean> c2 = trie2.getCursor();
		int size = 0;
		Node root1 = c1.getNode();
		Node root2 = c2.getNode();
		assertEquals(c1.getValue(), c2.getValue());
		TrieTraversal traversal = TrieTraversal.DEPTH_FIRST;
		while (traversal.moveToNextNode(root1, c1)) {
			++size;
			assertTrue(traversal.moveToNextNode(root2, c2));
			assertEquals(c1.getLabel(), c2.getLabel());
			assertEquals(c1.getValue(), c2.getValue());
		}
		c1.close();
		c2.close();
	}
}
