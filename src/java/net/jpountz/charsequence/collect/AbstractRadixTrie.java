package net.jpountz.charsequence.collect;

abstract class AbstractRadixTrie<T> extends AbstractTrie<T> implements RadixTrie<T> {

	public T put(char[] buffer, int offset, int length, T value) {
		RadixTrie.Cursor<T> cursor = getCursor();
		cursor.addChild(buffer, offset, length);
		T result = cursor.getValue();
		cursor.setValue(value);
		return result;
	}

	public T put(CharSequence sequence, int offset, int length, T value) {
		RadixTrie.Cursor<T> cursor = getCursor();
		cursor.addChild(sequence, offset, length);
		T result = cursor.getValue();
		cursor.setValue(value);
		return result;
	}
}
