package net.jpountz.trie;

import java.io.IOException;
import java.util.List;

public class MemoryBenchmark extends Benchmark {

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing argument: file");
		}
		List<char[]> words = readWords(args[0]);
		MemoryBenchmark pb = new MemoryBenchmark(words);
		int n = 5;
		for (TrieFactory<Boolean> factory : FACTORIES) {
			Stats stats = pb.test(factory, n);
			System.out.println(factory.newTrie().getClass().getSimpleName());
			System.out.print(" - after insert : ");
			System.out.println(stats.afterInsert);
			System.out.print(" - after trim   : ");
			System.out.println(stats.afterTrim);
		}
	}

	private final char[][] words;

	public MemoryBenchmark(List<char[]> words) {
		this.words = words.toArray(new char[words.size()][]);
	}

	private static class Stats {
		public long afterInsert;
		public long afterTrim;
	}

	private static void gc() throws InterruptedException {
		for (int i = 0; i < 5; ++i) {
			System.gc();
			Thread.sleep(500);
		}
		Thread.sleep(3000);
	}

	public Stats testMemory(TrieFactory<Boolean> factory) throws InterruptedException {
		factory.newTrie(); // to ensure static data structures are loaded
		Stats stats = new Stats();
		long before, afterInsert, afterTrim;
		gc();
		before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		Trie<Boolean> trie = factory.newTrie();
		for (char[] word : words) {
			trie.put(word, Boolean.TRUE);
		}
		gc();
		afterInsert = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		if (trie instanceof Trie.Trimmable) {
			((Trie.Trimmable) trie).trimToSize();
		}
		gc();
		afterTrim = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		stats.afterInsert = afterInsert - before;
		stats.afterTrim = afterTrim - before;
		trie.clear(); // to keep a reference on the trie
		return stats;
	}

	public Stats test(TrieFactory<Boolean> factory, int n) throws InterruptedException {
		Stats stats = new Stats();
		stats.afterInsert = Long.MAX_VALUE;
		stats.afterTrim = Long.MAX_VALUE;
		for (int i = 0; i < n; ++i) {
			Stats newStats = testMemory(factory);
			stats.afterInsert = Math.min(stats.afterInsert, newStats.afterInsert);
			stats.afterTrim = Math.min(stats.afterTrim, newStats.afterTrim);
		}
		return stats;
	}
}
