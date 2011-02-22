package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectSortedMap;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Trie based on a {@link Char2ObjectRBTreeMap}.
 */
public class Char2ObjectRBTreeMapTrie<T> extends AbstractChar2ObjectSortedMapTrie<T> {

	private static class Char2ObjectRBTreeMapTrieCursor<T> extends Char2ObjectSortedMapTrieCursor<T> {

		protected Char2ObjectRBTreeMapTrieCursor(
				AbstractChar2ObjectSortedMapTrie<T> trie,
				Char2ObjectSortedMapTrieNode<T> current,
				Deque<Char2ObjectSortedMapTrieNode<T>> parents) {
			super(trie, current, parents);
		}

		public Char2ObjectRBTreeMapTrieCursor(AbstractChar2ObjectSortedMapTrie<T> trie) {
			this(trie, trie.root, new ArrayDeque<Char2ObjectSortedMapTrieNode<T>>());
		}

		@Override
		protected Char2ObjectSortedMap<Char2ObjectSortedMapTrieNode<T>> newMap() {
			return new Char2ObjectRBTreeMap<Char2ObjectSortedMapTrieNode<T>>();
		}

		@Override
		public Char2ObjectSortedMapTrieCursor<T> clone() {
			return new Char2ObjectRBTreeMapTrieCursor<T>(trie, trie.root,
					new ArrayDeque<Char2ObjectSortedMapTrieNode<T>>(parents));
		}
		
	}

	@Override
	public Cursor<T> getCursor() {
		return new Char2ObjectRBTreeMapTrieCursor<T>(this);
	}

}
