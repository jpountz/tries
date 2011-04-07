package net.jpountz.charsequence.collect;

import it.unimi.dsi.fastutil.chars.CharCollection;
import net.jpountz.charsequence.collect.RadixTrie.LabelsInternable;

public class DelegatingTrie<T> extends AbstractTrie<T> implements Trie.Optimizable, Trie.Trimmable, RadixTrie.LabelsInternable {

	protected static class Cursor<T> implements Trie.Cursor<T> {

		protected final Trie.Cursor<T> delegate;

		public Cursor(Trie.Cursor<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Trie.Node getNode() {
			return delegate.getNode();
		}

		@Override
		public boolean isAtRoot() {
			return delegate.isAtRoot();
		}

		@Override
		public boolean isAt(Trie.Node node) {
			return delegate.isAt(node);
		}

		@Override
		public int depth() {
			return delegate.depth();
		}

		@Override
		public String getLabel() {
			return delegate.getLabel();
		}

		@Override
		public char getEdgeLabel() {
			return delegate.getEdgeLabel();
		}

		@Override
		public boolean moveToChild(char c) {
			return delegate.moveToChild(c);
		}

		@Override
		public boolean moveToFirstChild() {
			return delegate.moveToFirstChild();
		}

		@Override
		public boolean moveToBrother() {
			return delegate.moveToBrother();
		}

		@Override
		public void addChild(char c) {
			delegate.addChild(c);
		}

		@Override
		public boolean removeChild(char c) {
			return delegate.removeChild(c);
		}

		@Override
		public void removeChildren() {
			delegate.removeChildren();
		}

		@Override
		public boolean moveToParent() {
			return delegate.moveToParent();
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			delegate.getChildrenLabels(children);
		}

		@Override
		public int getChildrenSize() {
			return delegate.getChildrenSize();
		}

		@Override
		public T getValue() {
			return delegate.getValue();
		}

		@Override
		public void setValue(T value) {
			delegate.setValue(value);
		}

		@Override
		public void reset() {
			delegate.reset();
		}

	}

	protected final Trie<T> delegate;

	public DelegatingTrie(Trie<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Trie.Cursor<T> getCursor() {
		return delegate.getCursor();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public T put(char[] buffer, int offset, int length, T value) {
		return delegate.put(buffer, offset, length, value);
	}

	@Override
	public T put(CharSequence sequence, int offset, int length, T value) {
		return delegate.put(sequence, offset, length, value);
	}

	@Override
	public T remove(char[] buffer, int offset, int length) {
		return delegate.remove(buffer, offset, length);
	}

	@Override
	public T remove(CharSequence sequence, int offset, int length) {
		return delegate.remove(sequence, offset, length);
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		return delegate.get(buffer, offset, length);
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		return delegate.get(sequence, offset, length);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public void trimToSize() {
		if (delegate instanceof Trimmable) {
			((Trimmable) delegate).trimToSize();
		}
	}

	@Override
	public void optimizeFor(net.jpountz.charsequence.collect.Trie.Traversal traversal) {
		if (delegate instanceof Optimizable) {
			((Optimizable) delegate).optimizeFor(traversal);
		}
	}

	@Override
	public void internLabels(TrieFactory<char[]> labels) {
		if (delegate instanceof LabelsInternable) {
			((LabelsInternable) delegate).internLabels(labels);
		}
	}

	@Override
	public void internLabels(Trie<char[]> trie) {
		if (delegate instanceof LabelsInternable) {
			((LabelsInternable) delegate).internLabels(trie);
		}
	}

}
