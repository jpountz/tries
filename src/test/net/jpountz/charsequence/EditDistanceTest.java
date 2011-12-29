package net.jpountz.charsequence;

import junit.framework.TestCase;

public class EditDistanceTest extends TestCase {

	private EditWeight weight = new AbstractEditWeight() {

		@Override
		public double insertionCost(int offset, char c) {
			return 1 + 1d / (10 + offset + c);
		}

		@Override
		public double deletionCost(int offset, char c) {
			return 1 + 1d / (11 + offset + c);
		}

		@Override
		public double substitutionCost(int fromOffset, int toOffset, char c1, char c2) {
			return 1 + 1d / (12 + fromOffset + toOffset + c1 + c2);
		}

		@Override
		public double transpositionCost(int fromOffset, int toOffset, char c1, char c2) {
			return 1 + 1d / (13 + fromOffset + toOffset + c1 + c2);
		}
		
	};
	private EditDistance distance = new EditDistance(weight);

	public void testEmpty() {
		assertEquals(0d, distance.distance("", ""));
		assertEquals(weight.insertionCost(0, 'a'), distance.distance("", "a"));
		assertEquals(weight.insertionCost(0, 'a') + weight.insertionCost(1, 'b'), distance.distance("", "ab"));
		assertEquals(weight.deletionCost(0, 'a'), distance.distance("a", ""));
		assertEquals(weight.deletionCost(0, 'a') + weight.deletionCost(1, 'b'), distance.distance("ab", ""));
	}

	public void testEquals() {
		assertEquals(0d, distance.distance("a", "a"));
		assertEquals(0d, distance.distance("abcde", "abcde"));
		assertEquals(0d, distance.distance("aéÞ”", "aéÞ”"));
	}

	public void testInsertion() {
		assertEquals(weight.insertionCost(3, 'd'), distance.distance("abc", "abcd"));
		assertEquals(weight.insertionCost(0, 'a'), distance.distance("bcd", "abcd"));
		assertEquals(weight.insertionCost(2, 'c'), distance.distance("abd", "abcd"));
	}

	public void testDeletion() {
		assertEquals(weight.deletionCost(3, 'd'), distance.distance("abcd", "abc"));
		assertEquals(weight.deletionCost(0, 'a'), distance.distance("abcd", "bcd"));
		assertEquals(weight.deletionCost(2, 'c'), distance.distance("abcd", "abd"));
	}

	public void testSubstitution() {
		assertEquals(weight.substitutionCost(0, 0, 'a', 'b'), distance.distance("abcd", "bbcd"));
		assertEquals(weight.substitutionCost(3, 3, 'd', 'e'), distance.distance("abcd", "abce"));
		assertEquals(weight.substitutionCost(2, 2, 'c', 'd'), distance.distance("abcd", "abdd"));
	}

	public void testTransposition() {
		assertEquals(weight.transpositionCost(0, 0, 'a', 'b'), distance.distance("abcd", "bacd"));
		assertEquals(weight.transpositionCost(2, 2, 'c', 'd'), distance.distance("abcd", "abdc"));
	}

}
