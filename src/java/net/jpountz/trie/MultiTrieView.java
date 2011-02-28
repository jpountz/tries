package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharRBTreeSet;
import it.unimi.dsi.fastutil.chars.CharSortedSet;

/**
 * View on several tries. The first one has precedence.
 */
class MultiTrieView<T> extends AbstractTrie<T> {

	private static class MultiTrieViewNode implements Node {

		private final Node n1;
		private final Node n2;

		public MultiTrieViewNode(Node n1, Node n2) {
			this.n1 = n1;
			this.n2 = n2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((n1 == null) ? 0 : n1.hashCode());
			result = prime * result + ((n2 == null) ? 0 : n2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MultiTrieViewNode other = (MultiTrieViewNode) obj;
			if (n1 == null) {
				if (other.n1 != null)
					return false;
			} else if (!n1.equals(other.n1))
				return false;
			if (n2 == null) {
				if (other.n2 != null)
					return false;
			} else if (!n2.equals(other.n2))
				return false;
			return true;
		}

	}

	private static class MultiTrieViewCursor<T> extends AbstractCursor<T> {

		private final Cursor<T> c1;
		private final Cursor<T> c2;
		private Cursor<T> current;
		private final CharSortedSet children;

		public MultiTrieViewCursor(Cursor<T> c1, Cursor<T> c2) {
			this.c1 = c1;
			this.c2 = c2;
			current = c1;
			children = new CharRBTreeSet();
		}

		@Override
		protected CharSequence getLabelInternal() {
			return getLabel();
		}

		@Override
		public Node getNode() {
			return new MultiTrieViewNode(c1.getNode(), c2.getNode());
		}

		@Override
		public boolean isAtRoot() {
			return c1.isAtRoot() && c2.isAtRoot();
		}

		@Override
		public boolean isAt(Node n) {
			MultiTrieViewNode node = (MultiTrieViewNode) n;
			return c1.isAt(node.n1) && c2.isAt(node.n2);
		}

		@Override
		public int depth() {
			return current.depth();
		}

		@Override
		public String getLabel() {
			return current.getLabel();
		}

		@Override
		public char getEdgeLabel() {
			return current.getEdgeLabel();
		}

		private Cursor<T> getOther(Cursor<T> current) {
			if (current == c1) {
				return c2;
			} else {
				return c1;
			}
		}

		@Override
		public boolean moveToChild(char c) {
			final int depth = current.depth();
			final char edge = current.getEdgeLabel();
			Cursor<T> other = getOther(current);
			if (current.moveToChild(c)) {
				if (depth == other.depth() && edge == other.getEdgeLabel()) {
					other.moveToChild(c);
				}
				return true;
			} else if (depth == other.depth() && edge == other.getEdgeLabel()) {
				if (other.moveToChild(c)) {
					current = other;
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean moveToFirstChild() {
			Cursor<T> other = getOther(current);
			if (current.depth() > other.depth() ||
					current.getEdgeLabel() != other.getEdgeLabel()) {
				return current.moveToFirstChild();
			} else {
				children.clear();
				c1.getChildrenLabels(children);
				c2.getChildrenLabels(children);
				if (children.isEmpty()) {
					return false;
				} else {
					return moveToChild(children.firstChar());
				}
			}
		}

		@Override
		public boolean moveToBrother() {
			Cursor<T> other = getOther(current);
			if (current.depth() > other.depth()) {
				if (current.depth() == other.depth() + 1 &&
						other.moveToChild(current.getEdgeLabel())) {
						//other.moveToFirstChild()) {
					if (other.getEdgeLabel() < current.getEdgeLabel() ||
							(!current.moveToBrother() || other.getEdgeLabel() < current.getEdgeLabel())) {
						current = other;
					}
					return true;
				} else {
					return current.moveToBrother();
				}
			} else {
				char previousEdge = current.getEdgeLabel();
				if (current.moveToBrother()) {
					if (previousEdge == other.getEdgeLabel()) {
						if (other.moveToBrother()) {
							if (other.getEdgeLabel() < current.getEdgeLabel()) {
								current = other;
							}
						} else {
							return true;
						}
					}
					if (other.getEdgeLabel() < current.getEdgeLabel()) {
						current = other;
					}
					/*if ((previousEdge == other.getEdgeLabel() && other.moveToBrother() && other.getEdgeLabel() < current.getEdgeLabel()) ||
							other.getEdgeLabel() < current.getEdgeLabel()) {
						current = other;
					}*/
					return true;
				} else if (other.getEdgeLabel() > current.getEdgeLabel()) {
					current = other;
					return true;
				} else if (other.moveToBrother()) {
					current = other;
					return true;
				} else {
					return false;
				}
			}
		}

		@Override
		public void addChild(char c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeChild(char c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean moveToParent() {
			Cursor<T> other = getOther(current);
			if (current.depth() == other.depth()) {
				other.moveToParent();
			}
			return current.moveToParent();
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			Cursor<T> other = getOther(current);
			this.children.clear();
			current.getChildrenLabels(this.children);
			if (current.depth() == other.depth() &&
					current.getEdgeLabel() == other.getEdgeLabel()) {
				other.getChildrenLabels(this.children);
			}
			children.addAll(this.children);
		}

		@Override
		public int getChildrenSize() {
			children.clear();
			getChildrenLabels(children);
			return children.size();
		}

		@Override
		public T getValue() {
			Cursor<T> other = getOther(current);
			if (current.depth() == other.depth() &&
					current.getEdgeLabel() == other.getEdgeLabel()) {
				T result = c1.getValue();
				if (result == null) {
					result = c2.getValue();
				}
				return result;
			} else {
				return current.getValue();
			}
		}

		@Override
		public void setValue(T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {
			c1.reset();
			c2.reset();
		}

		@Override
		public MultiTrieViewCursor<T> clone() {
			return new MultiTrieViewCursor<T>(c1.clone(), c2.clone());
		}
	}

	private final Trie<T> t1;
	private final Trie<T> t2;

	public MultiTrieView(Trie<T> t1, Trie<T> t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	public void put(char[] buffer, int offset, int length, T value) {
		throw new UnsupportedOperationException();
	}

	public void put(CharSequence sequence, int offset, int length, T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		T result = t1.get(buffer, offset, length);
		if (result == null) {
			result = t2.get(buffer, offset, length);
		}
		return result;
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		T result = t1.get(sequence, offset, length);
		if (result == null) {
			result = t2.get(sequence, offset, length);
		}
		return result;
	}

	@Override
	public Cursor<T> getCursor() {
		return new MultiTrieViewCursor<T>(t1.getCursor(), t2.getCursor());
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

}
