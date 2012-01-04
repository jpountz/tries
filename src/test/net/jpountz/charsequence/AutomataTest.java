package net.jpountz.charsequence;

import it.unimi.dsi.fastutil.chars.CharAVLTreeSet;
import it.unimi.dsi.fastutil.chars.CharSet;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jpountz.charsequence.Automata.DistanceState;

public class AutomataTest extends TestCase {

	private static SortedSet<String> toCollection(Iterable<String> i) {
		TreeSet<String> result = new TreeSet<String>();
		for (String s : i) {
			result.add(s);
		}
		return result;
	}

	private static SortedSet<String> toCollection(String...strings) {
		return new TreeSet<String>(Arrays.asList(strings));
	}

	public void testForWord() {
		Automaton<Integer> automaton = Automata.forWord("tables");
		CharSet alphabet = new CharAVLTreeSet("abcdefgh tables".toCharArray());
		assertEquals(
				toCollection("tables"),
				toCollection(automaton.getDictionary(alphabet)));
	}

	public void testEditDistance() {
		Automaton<DistanceState> automaton = Automata.forEditWeight("ab",
				CommonEditWeight.DAMEREAU_LEVENSHTEIN, 1);
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		assertEquals(toCollection(
				"a", "aa", "aab", "ab", "aba", "abb", "abc", "ac", "acb",
				"b", "ba", "bab", "bb",
				"cab", "cb"),
				toCollection(automaton.getDictionary(alphabet)));
	}

	public void testReverse() {
		Automaton<DistanceState> automaton = Automata.forEditWeight("ab",
				CommonEditWeight.DAMEREAU_LEVENSHTEIN, 1);
		Automaton<DistanceState> automaton2 = automaton.reverse().reverse();
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		assertEquals(
				toCollection(automaton.getDictionary(alphabet)),
				toCollection(automaton2.getDictionary(alphabet)));
	}

	public void testDeteminize() {
		NFA<DistanceState> automaton = Automata.forEditWeight("abb",
				CommonEditWeight.DAMEREAU_LEVENSHTEIN, 1);
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		DFA<Set<DistanceState>> automaton2 = automaton.determinizePowerSet();
		assertEquals(
				toCollection(automaton.getDictionary(alphabet)),
				toCollection(automaton2.getDictionary(alphabet)));
		for (String word : toCollection(automaton.getDictionary(alphabet))) {
			assertTrue("Failed for " + word, automaton2.accept(word));
		}
	}

	private static DFA<Integer> newDFA() {
		DFA<Integer> dfa = new DFA<Integer>(0);
		dfa.addTransition(0, 1, 'a');
		dfa.addTransition(0, 2, 'b');
		dfa.addTransition(0, 3, 'a');
		dfa.addDefaultTransition(1, 4);
		dfa.addDefaultTransition(3, 4);
		dfa.addDefaultTransition(2, 4);
		dfa.setFinal(4);
		return dfa;
	}

	public void testMinimizeBrzozowski() {
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		DFA<Integer> dfa = newDFA(); dfa.addDefaultTransition(5, 6);
		//assertEquals(5, dfa.getNumberOfStates());
		DFA<Set<Set<Integer>>> dfa2 = dfa.minimizeBrzozowski();
		assertEquals(3, dfa2.getNumberOfStates());
		assertEquals(
				toCollection(dfa.getDictionary(alphabet)),
				toCollection(dfa2.getDictionary(alphabet)));
	}

	public void testMinimizeHopCroft() {
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		DFA<Integer> dfa = newDFA(); dfa.addDefaultTransition(5, 6);
		//assertEquals(5, dfa.getNumberOfStates());
		DFA<Set<Integer>> dfa2 = dfa.minimizeHopCroft();
		System.out.println(dfa2);
		assertEquals(3, dfa2.getNumberOfStates());
		assertEquals(
				toCollection(dfa.getDictionary(alphabet)),
				toCollection(dfa2.getDictionary(alphabet)));
	}

}
