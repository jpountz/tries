package net.jpountz.trie.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {

	public void testContactIterator() {
		Iterator<Integer> l1 = Collections.<Integer>emptyList().iterator();
		Iterator<Integer> l2 = Arrays.asList(1, 2, 3).iterator();
		Iterator<Integer> l3 = Collections.<Integer>emptySet().iterator();
		Iterator<Integer> l4 = Collections.<Integer>emptySet().iterator();
		Iterator<Integer> l5 = Collections.<Integer>singleton(4).iterator();
		Iterator<Integer> l6 = Collections.<Integer>emptySet().iterator();
		@SuppressWarnings("unchecked")
		Iterator<Integer> concat = Utils.concat(Arrays.asList(l1, l2, l3, l4, l5, l6).iterator());
		assertEquals(true, concat.hasNext());
		assertEquals(1, concat.next().intValue());
		assertEquals(true, concat.hasNext());
		assertEquals(2, concat.next().intValue());
		assertEquals(true, concat.hasNext());
		assertEquals(3, concat.next().intValue());
		assertEquals(true, concat.hasNext());
		assertEquals(4, concat.next().intValue());
		assertEquals(false, concat.hasNext());
	}

}
