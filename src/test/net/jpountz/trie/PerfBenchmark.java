package net.jpountz.trie;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PerfBenchmark extends Benchmark {

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing argument: file");
		}
		List<char[]> words = readWords(args[0]);
		PerfBenchmark pb = new PerfBenchmark(words);
		int n = 20;
		for (TrieFactory factory : FACTORIES) {
			System.gc();
			Thread.sleep(3000);
			Stats stats = pb.test(factory, n);
			System.out.println(factory.newTrie().getClass().getSimpleName());
			System.out.print(" - insert:    ");
			System.out.println(stats.insert);
			System.out.print(" - trim:      ");
			System.out.println(stats.trim);
			System.out.print(" - lookup:    ");
			System.out.println(stats.lookup);
			System.out.print(" - enumerate: ");
			if (stats.enumerate >= 0) {
				System.out.println(stats.enumerate);
			} else {
				System.out.println("unsupported");
			}
			System.out.print(" - neighbors: ");
			if (stats.neighbors >= 0) {
				System.out.println(stats.neighbors);
			} else {
				System.out.println("unsupported");
			}
		}
	}

	private final char[][] wordsToWrite;
	private final char[][] wordsToRead;

	public PerfBenchmark(List<char[]> words) {
		this.wordsToWrite = words.toArray(new char[words.size()][]);
		Collections.shuffle(words);
		this.wordsToRead = words.toArray(new char[words.size()][]);
	}

	public long testInsert(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < wordsToWrite.length; ++i) {
			trie.put(wordsToWrite[i], Boolean.TRUE);
		}
		return System.currentTimeMillis() - start;
	}

	public long testTrimToSize(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		trie.trimToSize();
		return System.currentTimeMillis() - start;
	}

	public long testLookup(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < wordsToRead.length; ++i) {
			if (trie.get(wordsToRead[i]) == null) {
				throw new NullPointerException(trie.getClass().getSimpleName() + " " + new String(wordsToRead[i]));
			}
		}
		return System.currentTimeMillis() - start;
	}

	public long testEnumerate(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		Trie.Cursor<Boolean> cursor = trie.getCursor();
		Trie.Node under = cursor.getNode();
		while (Tries.moveToNextSuffix(under, cursor)) {}
		return System.currentTimeMillis() - start;
	}

	public long testNeighbors(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		Set<Trie.Entry<Boolean>> neighbors = new HashSet<Trie.Entry<Boolean>>();
		for (int i = 0; i < wordsToRead.length; i+=100) {
			neighbors.clear();
			Tries.getNeighbors(wordsToRead[i], trie, 1, neighbors);
			if (neighbors.isEmpty()) {
				throw new Error("Badly implemented: no neighbor: " + new String(wordsToRead[i]));
			}
		}
		return System.currentTimeMillis() - start;
	}

	private static class Stats {
		public long insert = 0;
		public long trim   = 0;
		public long lookup = 0;
		public long enumerate = 0;
		public long neighbors = 0;
	}

	public Stats test(TrieFactory factory, int n) {
		Stats stats = new Stats();
		Trie<Boolean> trie = factory.newTrie();
		testInsert(trie);
		testTrimToSize(trie);
		testLookup(trie);
		try {
			testEnumerate(trie);
		} catch (UnsupportedOperationException e) { /* ignore */ }
		try {
			testNeighbors(trie);
		} catch (UnsupportedOperationException e) { /* ignore */ }
		for (int i = 0; i < n; ++i) {
			trie = factory.newTrie();
			stats.insert += testInsert(trie);
			stats.trim += testTrimToSize(trie);
			stats.lookup += testLookup(trie);
			try {
				stats.enumerate += testEnumerate(trie);
			} catch (UnsupportedOperationException e) {
				stats.enumerate -= 1;
			}
			try {
				stats.neighbors += testNeighbors(trie);
			} catch (UnsupportedOperationException e) {
				stats.neighbors -= 1;
			}
		}
		stats.insert /= n;
		stats.trim /= n;
		stats.lookup /= n;
		stats.enumerate /= n;
		stats.neighbors /= n;
		return stats;
	}

}
