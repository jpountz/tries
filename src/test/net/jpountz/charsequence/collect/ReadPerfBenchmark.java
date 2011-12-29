package net.jpountz.charsequence.collect;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.jpountz.charsequence.CharComparator;

import org.apache.lucene.util.RamUsageEstimator;

public class ReadPerfBenchmark extends Benchmark {

	private static final RamUsageEstimator RAM_USAGE_ESTIMATOR = new RamUsageEstimator();
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			throw new IllegalArgumentException("Arguments: writes, reads");
		}
		List<String> writes = readWords(args[0]);
		List<String> reads = readWords(args[1]);
		ReadPerfBenchmark pb = new ReadPerfBenchmark(writes, reads);
		int n = 5;
		for (CharSequenceMapFactory factory : CharSequenceMapFactory.values()) {
			System.gc();
			Thread.sleep(3000);
			Stats stats = pb.test(factory, n);
			System.out.println(factory.newMap().getClass().getSimpleName());
			System.out.print(" - insert:    ");
			System.out.println(stats.insert);
			System.out.print(" - lookup before optimize: ");
			System.out.println(stats.lookupUnoptimized);
			if (stats.trim > 0) {
				System.out.print(" - trim+optimize: ");
				System.out.println(stats.trim);
				System.out.print(" - lookup after optimize:  ");
				System.out.println(stats.lookup);
			}
			System.out.print(" - enumerate: ");
			System.out.println(stats.enumerate);
			System.out.print(" - memory:    ");
			System.out.println(
					RamUsageEstimator.humanReadableUnits(stats.memory,
							DECIMAL_FORMAT));
		}
	}

	private final String[] wordsToWrite;
	private final String[] wordsToRead;

	public ReadPerfBenchmark(List<String> writes, List<String> reads) {
		//Collections.shuffle(writes, new Random(0));
		wordsToWrite = writes.toArray(new String[writes.size()]);
		//Collections.shuffle(reads);
		wordsToRead = reads.toArray(new String[reads.size()]);
	}

	public long testInsert(Map<String, String> map) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < wordsToWrite.length; ++i) {
			map.put(wordsToWrite[i], Integer.toString(i));
		}
		return System.currentTimeMillis() - start;
	}

	@SuppressWarnings("unchecked")
	public long testTrimToSize(Map<String, String> map) {
		long start = System.currentTimeMillis();
		if (map instanceof Trie.Optimizable) {
			((Trie.Optimizable) map).optimizeFor(Trie.Traversal.BREADTH_FIRST_THEN_DEPTH);
		}
		if (map instanceof Trie.Trimmable) {
			((Trie.Trimmable) map).trimToSize();
		}
		if (map instanceof Trie.Compilable) {
			map = ((Trie.Compilable<String>) map).compile();
		}
		if (map instanceof RadixTrie.LabelsInternable) {
			((RadixTrie.LabelsInternable) map).internLabels(new TrieFactory<char[]>() {
				@Override
				public Trie<char[]> newTrie() {
					return new ListTrie<char[]>();
				}
				
			});
		}
		return System.currentTimeMillis() - start;
	}

	public long testLookup(Map<String, String> map) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < wordsToRead.length; ++i) {
			map.get(wordsToRead[i]);
		}
		return System.currentTimeMillis() - start;
	}

	public long testEnumerate(Map<String, String> map) {
		long start = System.currentTimeMillis();
		Set<Entry<String, String>> entries = map.entrySet();
		Iterator<Entry<String, String>> it = entries.iterator();
		while (it.hasNext()) { it.next(); }
		return System.currentTimeMillis() - start;
	}

	private static class Stats {
		public long insert = 0;
		public long lookupUnoptimized;
		public long trim   = 0;
		public long lookup = 0;
		public long enumerate = 0;
		public long memory = 0;
	}

	public Stats test(CharSequenceMapFactory factory, int n) {
		Stats stats = new Stats();
		Map<String, String> map = factory.newMap();
		if (!(map instanceof AbstractBinarySearchTrie))
			testInsert(map);
		testTrimToSize(map);
		testLookup(map);
		testEnumerate(map);
		for (int i = 0; i < n; ++i) {
			map = factory.newMap();
			if (map instanceof AbstractBinarySearchTrie) {
				stats.insert = -1;
				String[] keys = Arrays.copyOf(wordsToWrite, wordsToWrite.length);
				Arrays.sort(keys, CharComparator.DEFAULT.asCharSequenceComparator());
				map = Tries.sortedCharSequenceListAsTrie(Arrays.asList(keys), Arrays.asList(keys));
			} else {
				stats.insert += testInsert(map);
			}
			stats.lookupUnoptimized += testLookup(map);
			stats.trim += testTrimToSize(map);
			stats.lookup += testLookup(map);
			stats.enumerate += testEnumerate(map);
		}
		stats.memory = RAM_USAGE_ESTIMATOR.estimateRamUsage(map);
		stats.insert /= n;
		stats.lookupUnoptimized /= n;
		stats.trim /= n;
		stats.lookup /= n;
		stats.enumerate /= n;
		return stats;
	}

}
