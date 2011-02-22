package net.jpountz.trie.util;

import java.util.Iterator;

/**
 * Utility class.
 */
public class Utils {

	private Utils() {}

	private static class MultiIteratorWrapper<T> implements Iterator<T> {

		private final Iterator<Iterator<T>> wrapped;
		private Iterator<T> next;

		public MultiIteratorWrapper(Iterator<Iterator<T>> wrapped) {
			this.wrapped = wrapped;
			next = null;
		}

		@Override
		public boolean hasNext() {
			if (next == null) {
				while (wrapped.hasNext()) {
					next = wrapped.next();
					if (next.hasNext()) {
						return true;
					}
				}
				return false;
			} else {
				return next.hasNext();
			}
		}

		@Override
		public T next() {
			T result = next.next();
			if (!next.hasNext()) {
				next = null;
			}
			return result;
		}

		@Override
		public void remove() {
			next.remove();
		}

	}

	public static <T> Iterator<T> concat(Iterator<Iterator<T>> iterators) {
		return new MultiIteratorWrapper<T>(iterators);
	}

}
