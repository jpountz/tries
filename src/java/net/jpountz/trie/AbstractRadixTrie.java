package net.jpountz.trie;

abstract class AbstractRadixTrie<T> extends AbstractTrie<T> implements RadixTrie<T> {

	public void put(char[] buffer, int offset, int length, T value) {
		RadixTrie.Cursor<T> cursor = getCursor();
		cursor.addChild(buffer, offset, length);
		cursor.setValue(value);
	}

	public void put(CharSequence sequence, int offset, int length, T value) {
		RadixTrie.Cursor<T> cursor = getCursor();
		cursor.addChild(sequence, offset, length);
		cursor.setValue(value);
	}
}
