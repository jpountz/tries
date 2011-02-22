package net.jpountz.trie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.jpountz.trie.Trie.Entry;

public class PerfBenchmark {

	private static final List<TrieFactory<Boolean>> FACTORIES = new ArrayList<TrieFactory<Boolean>>();
	static {
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new HashMapTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new SimpleTrie<Boolean>();
			}
		});
		FACTORIES.add(new TrieFactory<Boolean>() {
			@Override
			public Trie<Boolean> newTrie() {
				return new FastCharMapTrie<Boolean>();
			}
		});
	}

	public static void main(String[] args) throws IOException, InterruptedException {
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
			if (trie.get(words[i]) == null) {
				throw new NullPointerException(trie.getClass().getSimpleName() + " " + new String(words[i]));
			}
		}
		return System.currentTimeMillis() - start;
	}

	public long testEnumerate(Trie<Boolean> trie) {
		long start = System.currentTimeMillis();
		Iterator<Entry<Boolean>> it = trie.getCursor().getSuffixes().iterator();
		while (it.hasNext()) {
			it.next();
		}
		return System.currentTimeMillis() - start;
	}

	private static class Stats {
		public long insert = 0;
		public long trim   = 0;
		public long lookup = 0;
		public long enumerate = 0;
	}

	public Stats test(TrieFactory<Boolean> factory, int n) {
		Stats stats = new Stats();
		Trie<Boolean> trie = factory.newTrie();
		testInsert(trie);
		testTrimToSize(trie);
		testLookup(trie);
		testEnumerate(trie);
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
		}
		stats.insert /= n;
		stats.trim /= n;
		stats.lookup /= n;
		stats.enumerate /= n;
		return stats;
	}

}
