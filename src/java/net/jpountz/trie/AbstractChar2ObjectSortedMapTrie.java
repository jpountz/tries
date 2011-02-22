package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectSortedMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

import net.jpountz.trie.util.PrefixedCharSequence;
import net.jpountz.trie.util.Utils;

/**
 * Trie based on a {@link Char2ObjectSortedMap}.
 *
 * @param <T> the value type
 */
public abstract class AbstractChar2ObjectSortedMapTrie<T> extends AbstractTrie<T> {

	protected static class Char2ObjectSortedMapTrieNode<T> {

		Char2ObjectSortedMap<Char2ObjectSortedMapTrieNode<T>> children;
		T value;
		public int size() {
			int result = 1;
			for (Char2ObjectSortedMapTrieNode<T> value: children.values()) {
				result += value.size();
			}
			return result;
		}
	}

	protected static abstract class Char2ObjectSortedMapTrieCursor<T> implements Cursor<T> {

		final AbstractChar2ObjectSortedMapTrie<T> trie;
		Char2ObjectSortedMapTrieNode<T> current;
		final Deque<Char2ObjectSortedMapTrieNode<T>> parents;

		protected Char2ObjectSortedMapTrieCursor(AbstractChar2ObjectSortedMapTrie<T> trie, Char2ObjectSortedMapTrieNode<T> current,
				Deque<Char2ObjectSortedMapTrieNode<T>> parents) {
			this.trie = trie;
			this.current = current;
			this.parents = parents;
		}

		protected abstract Char2ObjectSortedMap<Char2ObjectSortedMapTrieNode<T>> newMap();

		@Override
		public boolean moveToChild(char c) {
			if (current.children != null) {
				Char2ObjectSortedMapTrieNode<T> child = current.children.get(c);
				if (child == null) {
					return false;
				} else {
					parents.push(current);
					current = child;
					return true;
				}
			} else {
				return false;
			}
		}

		@Override
		public void addChild(char c) {
			parents.push(current);
			Char2ObjectSortedMapTrieNode<T> child;
			if (current.children == null) {
				current.children = newMap();
				child = new Char2ObjectSortedMapTrieNode<T>();
				current.children.put(c, child);
			} else {
				child = current.children.get(c);
				if (child == null) {
					child = new Char2ObjectSortedMapTrieNode<T>();
					current.children.put(c, child);
				}
			}
			current = child;
		}

		@Override
		public void removeChild(char c) {
			current.children.remove(c);
		}

		@Override
		public void getChildrenLabels(CharArrayList children) {
			if (current.children != null) {
				children.addAll(current.children.keySet());
			}
		}

		@Override
		public int getChildrenSize() {
			return current.children.size();
		}

		@Override
		public void getChildren(Char2ObjectMap<Cursor<T>> children) {
			if (current.children != null) {
				ObjectSortedSet<java.util.Map.Entry<Character, Char2ObjectSortedMapTrieNode<T>>> entryset = current.children.entrySet();
				for (Map.Entry<Character, Char2ObjectSortedMapTrieNode<T>> entry : entryset) {
					char c = entry.getKey();
					Char2ObjectSortedMapTrieNode<T> child = entry.getValue();
					Char2ObjectSortedMapTrieCursor<T> cursor = this.clone();
					cursor.current = child;
					cursor.parents.push(current);
					children.put(c, cursor);
				}
			}
		}

		private static class SuffixIterable<T> implements Iterable<Entry<T>> {

			private final Char2ObjectSortedMapTrieNode<T> root;
			private final CharSequence prefix;

			public SuffixIterable(Char2ObjectSortedMapTrieNode<T> root, CharSequence prefix) {
				this.root = root;
				this.prefix = prefix;
			}

			@Override
			public Iterator<Entry<T>> iterator() {
				if (root.children == null || root.children.isEmpty()) {
					if (root.value == null) {
						return Collections.<Entry<T>>emptySet().iterator();
					} else {
						return Collections.singleton(
								EntryImpl.newInstance(prefix, root.value)).iterator();
					}
				} else {
					int size = root.children.size();
					if (root.value != null) {
						++size;
					}
					Collection<Iterator<Entry<T>>> iterators = new ArrayList<Iterator<Entry<T>>>(size);
					if (root.value != null) {
						iterators.add(Collections.singleton(
								EntryImpl.newInstance(prefix, root.value)).iterator());
					}
					ObjectSortedSet<Map.Entry<Character, Char2ObjectSortedMapTrieNode<T>>> entryset = root.children.entrySet();
					for (Map.Entry<Character, Char2ObjectSortedMapTrieNode<T>> entry : entryset) {
						char c = entry.getKey();
						Char2ObjectSortedMapTrieNode<T> node = entry.getValue();
						iterators.add(new SuffixIterable<T>(node, new PrefixedCharSequence(prefix, c)).iterator());
					}
					return Utils.concat(iterators.iterator());
				}
			}

		}

		@Override
		public Iterable<Entry<T>> getSuffixes() {
			return new SuffixIterable<T>(current, "");
		}

		@Override
		public abstract Char2ObjectSortedMapTrieCursor<T> clone();

		public boolean moveToParent() {
			if (parents.isEmpty()) {
				return false;
			} else {
				current = parents.pop();
				return true;
			}
		}

		public T getValue() {
			return current.value;
		}

		public void setValue(T value) {
			current.value = value;
		}

		public int size() {
			return current.size();
		}

		public void reset() {
			current = trie.root;
			parents.clear();
		}
	}

	final Char2ObjectSortedMapTrieNode<T> root;

	public AbstractChar2ObjectSortedMapTrie() {
		this.root = new Char2ObjectSortedMapTrieNode<T>();
	}

	@Override
	public void clear() {
		root.value = null;
		root.children = null;
	}

}
