package net.jpountz.charsequence.collect;

import java.util.Map;

public interface CharSequenceMap<V> extends Map<String, V> {

	V get(CharSequence key, int offset, int length);
	V get(CharSequence key);
	V get(char[] key, int offset, int length);
	V get(char[] key);

	boolean containsKey(CharSequence key, int offset, int length);
	boolean containsKey(CharSequence key);
	boolean containsKey(char[] key, int offset, int length);
	boolean containsKey(char[] key);

	V put(CharSequence key, int offset, int length, V value);
	V put(CharSequence key, V value);
	V put(char[] key, int offset, int length, V value);
	V put(char[] key, V value);

	V remove(CharSequence key, int offset, int length);
	V remove(CharSequence key);
	V remove(char[] key, int offset, int length);
	V remove(char[] key);

}
