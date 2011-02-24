package net.jpountz.trie.util;

import junit.framework.TestCase;

public class FastCharMapTest extends TestCase {

	public void test() {
		FastCharMap<Boolean> m = new FastCharMap<Boolean>();
		assertEquals('\0', m.nextKey('\0'));
		assertEquals('\0', m.firstKey());
		assertEquals('\0', m.lastKey());
		m.put('h', Boolean.TRUE);
		m.put('b', Boolean.TRUE);
		m.put('l', Boolean.TRUE);
		assertFalse(m.remove('a'));
		assertFalse(m.remove('d'));
		assertFalse(m.remove('z'));
		assertEquals(3, m.size());
		assertTrue(m.remove('b'));
		assertEquals('h', m.firstKey());
		assertEquals('l', m.lastKey());
		assertEquals('h', m.nextKey('\0'));
		assertEquals('l', m.nextKey('h'));
		assertEquals('\0', m.nextKey('l'));
		assertEquals('\0', m.nextKey('z'));
		assertEquals('l', m.prevKey('z'));
		assertEquals('h', m.prevKey('l'));
		assertEquals('\0', m.prevKey('h'));
		assertTrue(m.remove('l'));
		assertEquals(1, m.size());
		m.remove('h');
		assertEquals('\0', m.nextKey('\0'));
		assertEquals('\0', m.firstKey());
		assertEquals('\0', m.lastKey());
		assertEquals('\0', m.nextKey('\0'));
		assertEquals('\0', m.firstKey());
		assertEquals(0, m.size());
	}

}
