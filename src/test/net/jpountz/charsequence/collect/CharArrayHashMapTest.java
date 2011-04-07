package net.jpountz.charsequence.collect;

public class CharArrayHashMapTest extends AbstractCharSequenceMapTest {

	@Override
	public CharSequenceMap<Integer> newMap() {
		return new CharArrayHashMap<Integer>();
	}

}
