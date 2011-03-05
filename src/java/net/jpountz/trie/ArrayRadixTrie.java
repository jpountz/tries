package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

/**
 * Radix trie based on backing arrays.
 */
abstract class ArrayRadixTrie<T> extends AbstractRadixTrie<T> {

	protected static final char[] EMPTY_CHAR_ARRAY = new char[0];

	private static class ArrayRadixTrieNode implements Node {

		final int parent;
		final int child;
		final int offset;

		public ArrayRadixTrieNode(int parent, int child, int offset) {
			this.parent = parent;
			this.child  = child;
			this.offset = offset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + child;
			result = prime * result + offset;
			result = prime * result + parent;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			ArrayRadixTrieNode other = (ArrayRadixTrieNode) obj;
			if (child != other.child)
				return false;
			if (offset != other.offset)
				return false;
			if (parent != other.parent)
				return false;
			return true;
		}

	}

	static final class ArrayRadixTrieCursor<T> extends AbstractCursor<T> implements RadixTrie.Cursor<T> {

		private StringBuilder label;
		final ArrayRadixTrie<T> trie;
		final IntArrayList parents;
		int parent, child, offset;

		protected ArrayRadixTrieCursor(ArrayRadixTrie<T> trie, int parent, int child, int offset,
				IntArrayList parents, StringBuilder label) {
			this.label = label;
			this.trie = trie;
			this.parent = parent;
			this.child = child;
			this.offset = offset; // offset of the next char to read in child
			this.parents = parents;
		}

		public ArrayRadixTrieCursor(ArrayRadixTrie<T> trie) {
			this(trie, START, NOT_FOUND, 0, new IntArrayList(), new StringBuilder());
		}

		@Override
		protected CharSequence getLabelInternal() {
			return label;
		}

		@Override
		public Node getNode() {
			return new ArrayRadixTrieNode(parent, child, offset);
		}

		private void moveToChildAux(char c) {
			++offset;
			if (trie.otherLabelCharsLength(child) + 1 == offset) {
				parents.push(parent);
				parent = child;
				offset = 0;
				child = NOT_FOUND;
			}
			label.append(c);
		}

		@Override
		public boolean moveToChild(char c) {
			if (offset == 0) {
				child = trie.childRadix(parent, c);
				if (child != NOT_FOUND) {
					moveToChildAux(c);
					return true;
				}
			} else {
				int ch = trie.otherLabelCharsBackend(child)[trie.otherLabelCharsOffset(child) + offset - 1];
				if (ch == c) {
					moveToChildAux(c);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean moveToFirstChildRadix() {
			if (offset == 0) {
				child = trie.firstChildRadix(child);
				if (child == NOT_FOUND) {
					return false;
				}
				label.append(trie.firstLabelChar(child));
				offset = 1;
			}
			char[] l = trie.otherLabelCharsBackend(child);
			label.append(l, trie.otherLabelCharsOffset(child) + offset - 1,
					trie.otherLabelCharsLength(child));
			parents.push(parent);
			parent = child;
			offset = 0;
			child = NOT_FOUND;
			return true;
		}

		@Override
		public boolean moveToFirstChild() {
			if (offset == 0) {
				child = trie.firstChildRadix(parent);
				if (child == NOT_FOUND) {
					return false;
				}
				label.append(trie.firstLabelChar(child));
			} else {
				label.append(trie.otherLabelCharsBackend(child)[trie.otherLabelCharsOffset(child) + offset - 1]);
			}
			++offset;
			if (trie.otherLabelCharsLength(child) + 1 == offset) {
				parents.push(parent);
				parent = child;
				offset = 0;
				child = NOT_FOUND;
			}
			return true;
		}

		@Override
		public boolean moveToBrother() {
			if (offset == 1) {
				int brother = trie.brother(child);
				if (brother != NOT_FOUND) {
					child = brother;
					label.setCharAt(label.length() - 1, trie.firstLabelChar(child));
					if (trie.otherLabelCharsLength(child) + 1 == offset) {
						parents.push(parent);
						parent = child;
						offset = 0;
						child = NOT_FOUND;
					}
					return true;
				}
			} else if (offset == 0 && trie.otherLabelCharsLength(parent) == 0) {
				int brother = trie.brother(parent);
				if (brother != NOT_FOUND) {
					parent = brother;
					label.setCharAt(label.length() - 1, trie.firstLabelChar(parent));
					if (trie.otherLabelCharsLength(parent) > 0) {
						child = parent;
						parent = parents.popInt();
						offset = 1;
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public void addChild(char c) {
			addChild(new char[] {c}, 0, 1);
		}

		public void addChild(char[] l, int off, int length) {
			while (length > 0 && moveToChild(l[off])) {
				++off;
				--length;
			}
			if (length == 0) {
				return;
			}
			int newNode = trie.newNode();
			parents.push(parent);
			if (offset == 0) {
				trie.addChildRadix(parent, newNode, l, off, length);
			} else {
				int newNode2 = trie.newNode();
				char[] childLabel = trie.label(child);
				trie.addChildRadix(parent, newNode2, childLabel, 0, offset);
				trie.addChildRadix(newNode2, child, childLabel, offset, childLabel.length - offset);
				trie.addChildRadix(newNode2, newNode, l, off, length);
				parents.push(child);
			}
			parent = newNode;
			offset = 0;
			label.append(l, off, length);
		}

		public void addChild(CharSequence l, int off, int length) {
			while (length > 0 && moveToChild(l.charAt(off))) {
				++off;
				--length;
			}
			if (length == 0) {
				return;
			}
			int newNode = trie.newNode();
			parents.push(parent);
			if (offset == 0) {
				trie.addChildRadix(parent, newNode, l, off, length);
			} else {
				int newNode2 = trie.newNode();
				char[] childLabel = trie.label(child);
				trie.addChildRadix(parent, newNode2, childLabel, 0, offset);
				trie.addChildRadix(newNode2, child, childLabel, offset, childLabel.length - offset);
				trie.addChildRadix(newNode2, newNode, l, off, length);
				parents.push(child);
			}
			parent = newNode;
			offset = 0;
			label.append(l, off, off+length);
		}

		@Override
		public boolean removeChild(char c) {
			// TODO
			return false;
		}

		@Override
		public void removeChildren() {
			// TODO
		}

		@Override
		public void getChildrenLabels(CharCollection children) {
			if (offset == 0) {
				int child = trie.firstChildRadix(parent);
				while (child != NOT_FOUND) {
					children.add(trie.firstLabelChar(child));
					child = trie.brother(child);
				}
			} else {
				children.add(trie.otherLabelCharsBackend(child)[trie.otherLabelCharsOffset(child) + offset-1]);
			}
		}

		@Override
		public int getChildrenSize() {
			if (offset == 0) {
				int result = 0;
				int child = trie.firstChildRadix(parent);
				while (child != NOT_FOUND) {
					++result;
					child = trie.brother(child);
				}
				return result;
			} else {
				return 1;
			}
		}

		@Override
		public boolean moveToParentRadix() {
			if (offset == 0) {
				if (parents.isEmpty()) {
					return false;
				} else {
					label.setLength(label.length() - trie.otherLabelCharsLength(parent) - 1);
					parent = parents.popInt();
					child = NOT_FOUND;
				}
			} else {
				label.setLength(label.length() - offset);
				offset = 0;
			}
			return true;
		}

		@Override
		public boolean moveToParent() {
			if (offset == 0) {
				if (parent == START) {
					return false;
				} else {
					offset = trie.otherLabelCharsLength(parent);
					if (offset == 0) {
						child = NOT_FOUND;
					} else {
						child = parent;
					}
					parent = parents.popInt();
				}
			} else {
				if (--offset == 0) {
					child = NOT_FOUND;
				}
			}
			label.setLength(label.length() - 1);
			return true;
		}

		@Override
		public boolean isAtRoot() {
			return parent == START && offset == 0;
		}

		@Override
		public boolean isAt(Node under) {
			ArrayRadixTrieNode node = (ArrayRadixTrieNode) under;
			return parent == node.parent && child == node.child && offset == node.offset;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getValue() {
			if (offset == 0) {
				return (T) trie.values[parent];
			} else {
				return null;
			}
		}

		@Override
		public void setValue(T value) {
			if (offset > 0) {
				int newNode = trie.newNode();
				char[] childLabel = trie.label(child);
				trie.addChildRadix(parent, newNode, childLabel, 0, offset);
				trie.addChildRadix(newNode, child, childLabel, offset, childLabel.length - offset);
				parents.push(parent);
				parent = newNode;
				offset = 0;
			}
			trie.values[parent] = value;
		}

		private int sizeUnder(int position) {
			int result = trie.otherLabelCharsLength(position) + 1;
			int child = trie.firstChildRadix(position);
			while (child != NOT_FOUND) {
				result += sizeUnder(child);
				child = trie.brother(child);
			}
			return result;
		}

		@Override
		public int radixSize() {
			if (offset == 0) {
				return sizeUnder(parent);
			} else {
				return sizeUnder(child) + trie.otherLabelCharsLength(child) + 1 - offset;
			}
		}

		private int radixSizeUnder(int position) {
			if (position == NOT_FOUND) {
				return 0;
			} else {
				int result = 1;
				int node = trie.firstChildRadix(position);
				while (node != NOT_FOUND) {
					result += sizeUnder(node);
					node = trie.brother(node);
				}
				return result;
			}
		}

		@Override
		public int size() {
			if (offset == 0) {
				return radixSizeUnder(parent);
			} else {
				return radixSizeUnder(child) + trie.otherLabelCharsLength(child) + 1 - offset;
			}
		}

		@Override
		public void reset() {
			parent = START;
			offset = 0;
			child = NOT_FOUND;
			parents.clear();
			label.setLength(0);
		}

		@Override
		public ArrayRadixTrieCursor<T> clone() {
			return new ArrayRadixTrieCursor<T>(trie, parent, child, offset,
					new IntArrayList(parents),
					new StringBuilder(label));
		}
	}

	protected static final int START = 0;
	protected static final int NOT_FOUND = -1;

	protected static final int DEFAULT_CAPACITY = 255;
	protected static final float DEFAULT_GROWTH_FACTOR = 2f;

	protected final float growthFactor;
	protected int size;
	protected Object[] values;
	protected int[] brothers;

	public ArrayRadixTrie(int initialCapacity, float growthFactor) {
		this(initialCapacity, growthFactor, 1, new Object[initialCapacity],
				new int[initialCapacity]);
		validate(initialCapacity, growthFactor);
		Arrays.fill(brothers, NOT_FOUND);
	}

	protected ArrayRadixTrie(int initialCapacity, float growthFactor, int size,
			Object[] values, int[] brothers) {
		validate(initialCapacity, growthFactor);
		this.growthFactor = growthFactor;
		this.size = size;
		this.values = values;
		this.brothers = brothers;
	}

	@Override
	public T get(char[] buffer, int offset, int length) {
		return getValue(getNode(buffer, offset, length));
	}

	@Override
	public T get(CharSequence sequence, int offset, int length) {
		return getValue(getNode(sequence, offset, length));
	}

	protected int getCapacity() {
		return values.length;
	}

	protected abstract int firstChildRadix(int position);

	protected int childRadix(int position, char label) {
		int child = firstChildRadix(position);
		while (child != NOT_FOUND) {
			char firstChar = firstLabelChar(child);
			if (firstChar == label) {
				return child;
			} else if (firstChar > label) {
				break;
			}
			child = brother(child);
		}
		return NOT_FOUND;
	}

	protected abstract void addChildRadix(int parent, int child, char[] label, int offset, int length);

	protected abstract void addChildRadix(int parent, int child, CharSequence sequence, int offset, int length);

	protected int brother(int position) {
		return brothers[position];
	}

	protected void setBrother(int position, int brother) {
		brothers[position] = brother;
	}

	protected abstract char firstLabelChar(int position);

	protected abstract char[] otherLabelCharsBackend(int position);

	protected abstract int otherLabelCharsOffset(int position);

	protected abstract int otherLabelCharsLength(int position);

	protected char[] label(int position) {
		return buildLabel(position);
	}

	protected char[] buildLabel(int position) {
		char[] result = new char[otherLabelCharsLength(position) + 1];
		result[0] = firstLabelChar(position);
		char[] otherChars = otherLabelCharsBackend(position);
		int offset = otherLabelCharsOffset(position);
		for (int i = 0; i < result.length - 1; ++i) {
			result[i+1] = otherChars[offset+i];
		}
		return result;
	}

	protected int getNode(char[] buffer, int offset, int length) {
		int node = START;
		while (length > 0) {
			node = childRadix(node, buffer[offset]);
			if (node != NOT_FOUND) {
				int otherCharsLength = otherLabelCharsLength(node);
				if (otherCharsLength >= length) {
					return NOT_FOUND;
				}
				if (otherCharsLength > 0) {
					char[] otherCharsBackend = otherLabelCharsBackend(node);
					int otherCharsOffset = otherLabelCharsOffset(node);
					for (int i = 0; i < otherCharsLength; ++i) {
						if (otherCharsBackend[otherCharsOffset+i] != buffer[offset+i+1]) {
							return NOT_FOUND;
						}
					}
				}
				int l = otherCharsLength + 1;
				offset += l;
				length -= l;
			} else {
				return NOT_FOUND;
			}
		}
		return node;
	}

	protected int getNode(CharSequence sequence, int offset, int length) {
		int node = START;
		while (length > 0) {
			node = childRadix(node, sequence.charAt(offset));
			if (node != NOT_FOUND) {
				int otherCharsLength = otherLabelCharsLength(node);
				if (otherCharsLength >= length) {
					return NOT_FOUND;
				}
				if (otherCharsLength > 0) {
					char[] otherCharsBackend = otherLabelCharsBackend(node);
					int otherCharsOffset = otherLabelCharsOffset(node);
					for (int i = 0; i < otherCharsLength; ++i) {
						if (otherCharsBackend[otherCharsOffset+i] != sequence.charAt(offset+i+1)) {
							return NOT_FOUND;
						}
					}
				}
				int l = otherCharsLength + 1;
				offset += l;
				length -= l;
			} else {
				return NOT_FOUND;
			}
		}
		return node;
	}

	public void ensureCapacity(int capacity) {
		int previousCapacity = getCapacity();
		if (capacity > previousCapacity) {
			brothers = Arrays.copyOf(brothers, capacity);
			Arrays.fill(brothers, previousCapacity, capacity, NOT_FOUND);
			values = Arrays.copyOf(values, capacity);
		}
	}

	public void trimToSize() {
		brothers = Arrays.copyOf(brothers, size);
		values = Arrays.copyOf(values, size);
	}

	@Override
	public void clear() {
		Arrays.fill(brothers, NOT_FOUND);
		Arrays.fill(values, null);
	}

	protected int newNode() {
		int capacity = getCapacity();
		if (size >= capacity) {
			ensureCapacity((int) Math.ceil(growthFactor * capacity));
		}
		return size++;
	}

	@Override
	public ArrayRadixTrieCursor<T> getCursor() {
		return new ArrayRadixTrieCursor<T>(this);
	}

	@SuppressWarnings("unchecked")
	public T getValue(int node) {
		if (node == NOT_FOUND) {
			return null;
		} else {
			return (T) values[node];
		}
	}

	@Override
	public int radixSize() {
		return size;
	}

}
