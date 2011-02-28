package net.jpountz.trie;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import net.jpountz.trie.util.IOUtils;
import net.jpountz.trie.util.Serializer;

public class DiskBasedTrie<T> extends AbstractTrie<T> {

	private static class DiskBasedNode implements Node {

		private final String label;

		public DiskBasedNode(String label) {
			this.label = label;
		}

		@Override
		public int hashCode() {
			return label.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DiskBasedNode other = (DiskBasedNode) obj;
			if (!label.equals(other.label))
				return false;
			return true;
		}

	}

	public static class DiskBasedCursor<T> extends AbstractCursor<T> {

		private final StringBuilder label;
		private final DiskBasedTrie<T> trie;
		private final IntArrayList parents;
		private final File file;
		private final FileChannel channel;
		private final DataInputStream is;

		private char edge;
		private int currentOffset;
		private Char2IntMap children;
		private int firstChildOffset;
		private int brotherOffset;
		private boolean valueRead;
		private T value;

		private DiskBasedCursor(DiskBasedTrie<T> trie, File file, IntArrayList parents, int currentOffset, int firstChildOffset, int brotherOffset,
				StringBuilder label, Char2IntMap children) throws FileNotFoundException, IOException {
			this.label = label;
			this.trie = trie;
			this.file = file;
			this.parents = parents;
			this.currentOffset = currentOffset;
			this.children = children;
			children.defaultReturnValue(0);
			this.firstChildOffset = firstChildOffset;
			this.brotherOffset = brotherOffset;
			valueRead = false;
			value = null;

			FileInputStream fileInputStream = new FileInputStream(file);
			channel = fileInputStream.getChannel();
			is = new DataInputStream(fileInputStream);
			moveToOffset(currentOffset);
		}

		public DiskBasedCursor(DiskBasedTrie<T> trie, File file) throws FileNotFoundException, IOException {
			this(trie, file, new IntArrayList(), 0, 0, 0, new StringBuilder(), new Char2IntArrayMap());
		}

		public void close() {
			try {
				is.close();
			} catch (IOException e) {}
		}

		@Override
		protected CharSequence getLabelInternal() {
			return label;
		}

		@Override
		public Node getNode() {
			return new DiskBasedNode(getLabel());
		}

		private void moveToOffset(final int offset) {
			try {
				channel.position(offset);
				edge = (char) IOUtils.readVInt(is);
				int childrenSize = IOUtils.readVInt(is);
				if (childrenSize == 0) {
					firstChildOffset = 0;
				}
				children.clear();
				char previous = '\0';
				for (int i = 0; i < childrenSize; ++i) {
					int n = IOUtils.readVInt(is);
					char c = (char) (n + previous);
					int childOffset = IOUtils.readInt(is);
					if (i == 0) {
						firstChildOffset = childOffset;
					}
					children.put(c, childOffset);
					previous = c;
				}
				brotherOffset = IOUtils.readInt(is);
				currentOffset = offset;
				valueRead = false;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private T readValue() {
			try {
				return trie.serializer.read(is);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean moveToChild(char c) {
			int offset = children.get(c);
			if (offset > 0) {
				parents.push(currentOffset);
				moveToOffset(offset);
				label.append(edge);
				assert c == edge;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean moveToFirstChild() {
			if (firstChildOffset > 0) {
				parents.push(currentOffset);
				moveToOffset(firstChildOffset);
				label.append(edge);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean moveToBrother() {
			if (brotherOffset > 0) {
				moveToOffset(brotherOffset);
				label.setCharAt(label.length() - 1, edge);
				return true;
			} else {
				return false;
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
			if (!parents.isEmpty()) {
				int parentOffset = parents.popInt();
				moveToOffset(parentOffset);
				label.setLength(label.length() - 1);
				return true;
			}
			return false;
		}

		@Override
		public T getValue() {
			if (!valueRead) {
				try {
					value = readValue();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				valueRead = true;
			}
			return (T) value;
		}

		@Override
		public void setValue(T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() {
			moveToOffset(0);
		}

		@Override
		public DiskBasedCursor<T> clone() {
			try {
				return new DiskBasedCursor<T>(trie, file, new IntArrayList(parents),
						currentOffset, firstChildOffset, brotherOffset, new StringBuilder(label), new Char2IntArrayMap(children));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	private final Serializer<T> serializer;
	private long version;
	private final File dir;
	private File current;
	private final File tmpFile;
	private CompactArrayTrie<T> unCommitted = new CompactArrayTrie<T>();
	private final TrieTraversal traversal;

	/**
	 * Create a new {@link DiskBasedTrie}.
	 *
	 * @param dir the directory to store the trie
	 * @param serializer the values serializer
	 * @param traversal the traversal the trie will be optimized for
	 * @throws IOException
	 */
	public DiskBasedTrie(File dir, Serializer<T> serializer,
			TrieTraversal traversal) throws IOException {
		this.serializer = serializer;
		this.traversal = traversal;
		version = 0;
		this.dir = dir;
		tmpFile = new File(dir, "tmp.trie");
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IOException(dir + " exists but is not a directory");
			}
		} else {
			if (!dir.mkdirs()) {
				throw new IOException("Could not create " + dir);
			}
		}
		String[] files = dir.list();
		Arrays.sort(files);
		String currentPath = null;
		if (files.length > 1) {
			throw new IOException("Found several files in " + dir);
		}
		if (files.length > 0) {
			currentPath = files[0];
			if (!currentPath.endsWith(".trie")) {
				throw new IOException("Unexpected file in " + dir + ": " + currentPath);
			}
			current = new File(dir, currentPath);
			String fileName = current.getName();
			int idx = fileName.indexOf('.');
			try {
				version = Long.valueOf(fileName.substring(0, idx));
			} catch (NumberFormatException e) {
				throw new IOException("File should be named <version>.trie");
			}
		} else {
			current = new File(dir, version + ".trie");
		}
		if (!current.exists()) {
			commit();
		}
	}

	@Override
	public DiskBasedCursor<T> getCursor() {
		if (current.exists()) {
			try {
				return new DiskBasedCursor<T>(this, current);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new IllegalStateException("Missing file");
		}
	}

	@Override
	public void clear() {
		if (current.exists() && !current.delete()) {
			throw new RuntimeException(("Could not remove the file"));
		}
	}

	@Override
	public void trimToSize() {
		unCommitted.trimToSize();
	}

	@Override
	public void put(char[] buffer, int offset, int length, T value) {
		unCommitted.put(buffer, offset, length, value);
	}

	@Override
	public void put(CharSequence sequence, int offset, int length, T value) {
		unCommitted.put(sequence, offset, length, value);
	}

	public void commit() throws IOException {
		if (tmpFile.exists() && !tmpFile.delete()) {
			throw new IOException("Could not remove " + tmpFile);
		}

		DataOutputStream os = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(tmpFile)));

		AbstractTrie<T> toSerialize = unCommitted;
		File previous = null;
		File dest = current;
		if (current.exists()) {
			toSerialize = new MultiTrieView<T>(unCommitted, this);
			dest = new File(dir, ++version + ".trie");
			previous = current;
		}

		try {
			toSerialize.serialize(os, serializer, traversal);
			os.flush();
		} finally {
			os.close();
		}

		tmpFile.renameTo(dest);
		current = dest;
		if (previous != null && !previous.delete()) {
			throw new IOException("Could not delete " + previous);
		}
	}

}
