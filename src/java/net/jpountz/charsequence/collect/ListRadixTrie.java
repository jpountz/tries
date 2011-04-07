package net.jpountz.charsequence.collect;

import java.util.Arrays;

import net.jpountz.charsequence.GrowthStrategy;

/**
 * A Radix Trie.
 *
 * This class has the same implementation principles as {@link ListTrie}
 * but applied to a patricia trie. This implementation may be more
 * memory-efficent but slower than a {@link ListTrie} for most use-cases.
 *
 * @param <T> the values type
 */
public class ListRadixTrie<T> extends AbstractListRadixTrie<T> implements Trie.Optimizable, Trie.Trimmable, RadixTrie.LabelsInternable {

	protected int[] children;
	protected char[][] labels;

	protected ListRadixTrie(int initialCapacity, GrowthStrategy growthStrategy,
			int size, Object[] values, int[] brothers) {
		super(initialCapacity, growthStrategy, size, values, brothers);
	}

	public ListRadixTrie(int initialCapacity, GrowthStrategy growthStrategy) {
		super(initialCapacity, growthStrategy);
		children = new int[initialCapacity];
		Arrays.fill(children, NOT_FOUND);
		labels = new char[initialCapacity][];
		labels[0] = EMPTY_CHAR_ARRAY;
	}

	public ListRadixTrie() {
		this(DEFAULT_CAPACITY, GrowthStrategy.FAST_GROWTH);
	}

	@Override
	protected char firstLabelChar(int position) {
		return labels[position][0];
	}

	@Override
	protected char[] otherLabelCharsBackend(int position) {
		return labels[position];
	}

	@Override
	protected int otherLabelCharsOffset(int position) {
		return 1;
	}

	@Override
	protected int otherLabelCharsLength(int position) {
		return labels[position].length - 1;
	}

	@Override
	protected char[] label(int position) {
		return labels[position];
	}

	@Override
	protected int firstChildRadix(int position) {
		return children[position];
	}

	protected void setLabel(int position, char[] label, int offset, int length) {
		labels[position] = Arrays.copyOfRange(label, offset, offset+length);
	}

	protected void setLabel(int position, CharSequence label, int offset, int length) {
		labels[position] = toArray(label, offset, length);
	}

	protected char[] toArray(CharSequence sequence, int offset, int length) {
		char[] array = new char[length];
		for (int i = 0; i < length; ++i) {
			array[i] = sequence.charAt(offset+i);
		}
		return array;
	}

	@Override
	protected void addChildRadix(int parent, int child, char[] label,
			int offset, int length) {
		int previousChild = NOT_FOUND;
		int nextChild = firstChildRadix(parent);
		while (nextChild != NOT_FOUND) {
			char nextChildFirstChar = firstLabelChar(nextChild);
			if (nextChildFirstChar > label[offset]) {
				break;
			} else if (nextChildFirstChar == label[offset]) {
				nextChild = brother(nextChild);
				continue;
			}
			previousChild = nextChild;
			nextChild = brother(nextChild);
		}
		if (previousChild == NOT_FOUND) {
			children[parent] = child;
		} else {
			setBrother(previousChild, child);
		}
		setBrother(child, nextChild);
		setLabel(child, label, offset, length);
	}

	@Override
	protected void addChildRadix(int parent, int child, CharSequence sequence,
			int offset, int length) {
		int previousChild = NOT_FOUND;
		int nextChild = firstChildRadix(parent);
		while (nextChild != NOT_FOUND) {
			char[] nextChildLabel = label(nextChild);
			if (nextChildLabel[0] > sequence.charAt(offset)) {
				break;
			} else if (nextChildLabel[0] == sequence.charAt(offset)) {
				nextChild = brother(nextChild);
				continue;
			}
			previousChild = nextChild;
			nextChild = brother(nextChild);
		}
		if (previousChild == NOT_FOUND) {
			children[parent] = child;
		} else {
			setBrother(previousChild, child);
		}
		setBrother(child, nextChild);
		setLabel(child, sequence, offset, length);
	}

	@Override
	public void ensureCapacity(int capacity) {
		int previousCapacity = getCapacity();
		if (capacity > previousCapacity) {
			super.ensureCapacity(capacity);
			children = Arrays.copyOf(children, capacity);
			Arrays.fill(children, previousCapacity, capacity, NOT_FOUND);
			labels = Arrays.copyOf(labels, capacity);
		}
	}

	@Override
	public void trimToSize() {
		super.trimToSize();
		children = Arrays.copyOf(children, size);
		labels = Arrays.copyOf(labels, size);
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(children, NOT_FOUND);
	}

	public void optimizeFor(Trie.Traversal traversal) {
		ListRadixTrie<T> trie = new ListRadixTrie<T>(radixSize(), growthStrategy);
		ArrayRadixTrieCursor<T> cursor = getCursor();
		Node node = cursor.getNode();
		do {
			trie.put(cursor.getLabelInternal(), cursor.getValue());
		} while (Tries.moveToNextSuffix(node, cursor, traversal));
		this.brothers = trie.brothers;
		this.children = trie.children;
		this.labels = trie.labels;
		this.values = trie.values;
	}

	public void internLabels(TrieFactory<char[]> factory) {
		internLabels(factory.newTrie());
	}

	public void internLabels(Trie<char[]> trie) {
		for (int i = 0; i < size; ++i) {
			char[] label = labels[i];
			char[] shared = trie.get(label);
			if (shared == null) {
				shared = label;
				trie.put(shared, shared);
			} else {
				labels[i] = shared;
			}
		}
	}

/*	public RadixTrie<T> compile() {
		CharArrayList firstChars = new CharArrayList();
		CharArrayList otherChars = new CharArrayList();
		IntArrayList otherCharsOffsets = new IntArrayList();
		for (int i = 0; i < size; ++i) {
			otherCharsOffsets.add(otherChars.size());
			if (i == START) {
				firstChars.add('\0');
			} else {
				firstChars.add(firstLabelChar(i));
			}
			int length = otherLabelCharsLength(i);
			int offset = otherLabelCharsOffset(i);
			for (int j = 0; j < length; ++j) {
				otherChars.add(otherLabelCharsBackend(i)[offset+j]);
			}
		}
		otherCharsOffsets.add(otherChars.size());
		return new CompiledArrayRadixTrie<T>(size,
				Arrays.copyOf(values, size),
				Arrays.copyOf(children, size),
				Arrays.copyOf(brothers, size),
				firstChars.toCharArray(),
				otherChars.toCharArray(),
				otherCharsOffsets.toIntArray());
	}

	private static class CompiledArrayRadixTrie<T> extends ArrayRadixTrie<T> {

		private int[] children;
		private char[] firstChars;
		private char[] otherCharsBackend;
		private int[] otherCharsOffsets;

		public CompiledArrayRadixTrie(int size, Object[] values, int[] children,
				int[] brothers, char[] firstChars, char[] otherCharsBackend,
				int[] otherCharsOffsets) {
			super(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR, size, values, brothers);
			this.children = children;
			this.firstChars = firstChars;
			this.otherCharsBackend = otherCharsBackend;
			this.otherCharsOffsets = otherCharsOffsets;
		}

		@Override
		protected int firstChildRadix(int position) {
			return children[position];
		}

		@Override
		protected void addChildRadix(int parent, int child, char[] label,
				int offset, int length) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void addChildRadix(int parent, int child,
				CharSequence sequence, int offset, int length) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected char firstLabelChar(int position) {
			return firstChars[position];
		}

		@Override
		protected char[] otherLabelCharsBackend(int position) {
			return otherCharsBackend;
		}

		@Override
		protected int otherLabelCharsOffset(int position) {
			return otherCharsOffsets[position];
		}

		@Override
		protected int otherLabelCharsLength(int position) {
			return otherCharsOffsets[position + 1] - otherCharsOffsets[position];
		}

	}*/

}
