package net.jpountz.charsequence;

import junit.framework.TestCase;

public class EditDistanceTest extends TestCase {

	private EditDistance distance = new EditDistance() {

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

	public void testEmpty() {
		assertEquals(0d, distance.distance("", ""));
		assertEquals(distance.insertionCost(0, 'a'), distance.distance("", "a"));
		assertEquals(distance.insertionCost(0, 'a') + distance.insertionCost(1, 'b'), distance.distance("", "ab"));
		assertEquals(distance.deletionCost(0, 'a'), distance.distance("a", ""));
		assertEquals(distance.deletionCost(0, 'a') + distance.deletionCost(1, 'b'), distance.distance("ab", ""));
	}

	public void testEquals() {
		assertEquals(0d, distance.distance("a", "a"));
		assertEquals(0d, distance.distance("abcde", "abcde"));
		assertEquals(0d, distance.distance("aéÞ”", "aéÞ”"));
	}

	public void testInsertion() {
		assertEquals(distance.insertionCost(3, 'd'), distance.distance("abc", "abcd"));
		assertEquals(distance.insertionCost(0, 'a'), distance.distance("bcd", "abcd"));
		assertEquals(distance.insertionCost(2, 'c'), distance.distance("abd", "abcd"));
	}

	public void testDeletion() {
		assertEquals(distance.deletionCost(3, 'd'), distance.distance("abcd", "abc"));
		assertEquals(distance.deletionCost(0, 'a'), distance.distance("abcd", "bcd"));
		assertEquals(distance.deletionCost(2, 'c'), distance.distance("abcd", "abd"));
	}

	public void testSubstitution() {
		assertEquals(distance.substitutionCost(0, 0, 'a', 'b'), distance.distance("abcd", "bbcd"));
		assertEquals(distance.substitutionCost(3, 3, 'd', 'e'), distance.distance("abcd", "abce"));
		assertEquals(distance.substitutionCost(2, 2, 'c', 'd'), distance.distance("abcd", "abdd"));
	}

	public void testTransposition() {
		assertEquals(distance.transpositionCost(0, 0, 'a', 'b'), distance.distance("abcd", "bacd"));
		assertEquals(distance.transpositionCost(2, 2, 'c', 'd'), distance.distance("abcd", "abdc"));
	}

}
