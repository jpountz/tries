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

	CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});

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
		AbstractFA<Integer> automaton = Automata.forWord("tables");
		CharSet alphabet = new CharAVLTreeSet("abcdefgh tables".toCharArray());
		assertEquals(
				toCollection("tables"),
				toCollection(automaton.getDictionary(alphabet)));
	}

	public void testEditDistance() {
		AbstractFA<DistanceState> automaton = Automata.forEditWeight("ab",
				CommonEditWeight.DAMEREAU_LEVENSHTEIN, 1);
		assertEquals(toCollection(
				"a", "aa", "aab", "ab", "aba", "abb", "abc", "ac", "acb",
				"b", "ba", "bab", "bb",
				"cab", "cb"),
				toCollection(automaton.getDictionary(alphabet)));
	}

	public void testRemoveUselessStates(AbstractFA<Object> automaton) {
		automaton.addTransition(0, 1, 'd');
		automaton.addDefaultTransition(1, 2);
		automaton.addDefaultTransition(0, 3);
		automaton.addTransition(3, 4, 'b');
		automaton.addTransition(5, 6, 'c');
		automaton.addDefaultTransition(6, 2);
		automaton.addTransition(2, 7, 'a');
		automaton.addDefaultTransition(7, 2);
		automaton.addTransition(0, 2, 'b');
		automaton.addFinal(2);
		assertEquals(8, automaton.getNumberOfStates());
		automaton.removeUselessStates();
		assertEquals(4, automaton.getNumberOfStates());
		assertTrue(automaton.accept("da"));
		assertTrue(automaton.accept("dd"));
		assertTrue(automaton.accept("b"));
	}

	public void testRemoveUselessStatesNFA() {
		testRemoveUselessStates(new NFA<Object>(0));
	}

	public void testRemoveUselessStatesDFA() {
		testRemoveUselessStates(new DFA<Object>(0));
	}

	public void testReverse() {
		AbstractFA<DistanceState> automaton = Automata.forEditWeight("ab",
				CommonEditWeight.DAMEREAU_LEVENSHTEIN, 1);
		AbstractFA<DistanceState> automaton2 = automaton.reverse().reverse();
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
		dfa.addDefaultTransition(5, 6);
		dfa.addFinal(4);
		return dfa;
	}

	public void testMinimizeBrzozowski() {
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		DFA<Integer> dfa = newDFA();
		assertEquals(7, dfa.getNumberOfStates());
		DFA<Set<Set<Integer>>> dfa2 = dfa.minimizeBrzozowski();
		assertEquals(3, dfa2.getNumberOfStates());
		assertEquals(
				toCollection(dfa.getDictionary(alphabet)),
				toCollection(dfa2.getDictionary(alphabet)));
	}

	public void testMinimizeHopCroft() {
		CharSet alphabet = new CharAVLTreeSet(new char[] {'a', 'b', 'c'});
		DFA<Integer> dfa = newDFA();
		assertEquals(7, dfa.getNumberOfStates());
		dfa.removeUselessStates();
		DFA<Set<Integer>> dfa2 = dfa.minimizeHopCroft();
		assertEquals(3, dfa2.getNumberOfStates());
		assertEquals(
				toCollection(dfa.getDictionary(alphabet)),
				toCollection(dfa2.getDictionary(alphabet)));
	}

}
