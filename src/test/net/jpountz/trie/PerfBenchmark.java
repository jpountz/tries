package net.jpountz.trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PerfBenchmark {

	private static final List<TrieFactory<Boolean>> FACTORIES = new ArrayList<TrieFactory<Boolean>>();
	static {
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new SimpleTrie<Boolean>();
			}
		});
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing argument: file");
		}
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		List<char[]> words = new ArrayList<char[]>();
		String line;
		while ((line = reader.readLine()) != null) {
			char[] buffer = new char[line.length()];
			line.getChars(0, line.length(), buffer, 0);
			words.add(buffer);
		}
		PerfBenchmark pb = new PerfBenchmark(words);
		int n = 20;
		for (TrieFactory<Boolean> factory : FACTORIES) {
			Stats stats = pb.test(factory, n);
			System.out.println(factory.newTrie().getClass().getSimpleName());
			System.out.print(" - insert: ");
			System.out.println(stats.insert);
			System.out.print(" - trim:   ");
			System.out.println(stats.trim);
			System.out.print(" - lookup: ");
			System.out.println(stats.lookup);
		}
	}

	private final char[][] words;

	public PerfBenchmark(List<char[]> words) {
		this.words = words.toArray(new char[words.size()][]);
	}

	public long testInsert(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < words.length; ++i) {
			trie.put(words[i], Boolean.TRUE);
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
		for (int i = 0; i < words.length; ++i) {
			trie.get(words[i]);
		}
		return System.currentTimeMillis() - start;
	}

	private static class Stats {
		public long insert = 0;
		public long trim   = 0;
		public long lookup = 0;
	}

	public Stats test(TrieFactory<Boolean> factory, int n) {
		Stats stats = new Stats();
		for (int i = 0; i < n; ++i) {
			Trie<Boolean> trie = factory.newTrie();
			stats.insert += testInsert(trie);
			stats.trim += testTrimToSize(trie);
			stats.lookup += testLookup(trie);
		}
		stats.insert /= n;
		stats.trim /= n;
		stats.lookup /= n;
		return stats;
	}

}