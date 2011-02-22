package net.jpountz.trie;

/**
 * A trie. http://en.wikipedia.org/wiki/Trie
 */
public interface Trie<T> {

	Cursor<T> getCursor();

	void put(char[] buffer, int offset, int length, T value);
	void put(char[] buffer, T value);
	void put(CharSequence sequence, int offset, int length, T value);
	void put(CharSequence sequence, T value);

	void remove(char[] buffer, int offset, int length);
	void remove(char[] buffer);
	void remove(CharSequence sequence, int offset, int length);
	void remove(CharSequence sequence);

	T get(char[] buffer, int offset, int length);
	T get(char[] buffer);
	T get(CharSequence sequence, int offset, int length);
	T get(CharSequence sequence);

	int size();
	void trimToSize();
}
