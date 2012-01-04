package net.jpountz.charsequence;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharSet;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.jpountz.charsequence.util.ImmutableSet;

/**
 * A deterministic finite-state machine.
 */
public class DFA<State> extends Automaton<State> {

	static class DFAStateWrapper<State> extends Automaton.StateWrapper<State> {

		final Char2ObjectMap<DFAStateWrapper<State>> mappedTransitions;

		public DFAStateWrapper(State state, CharComparator comparator) {
			super(state);
			this.mappedTransitions = new Char2ObjectRBTreeMap<DFAStateWrapper<State>>(comparator);
		}

		@Override
		public CharSet getMappedChars() {
			return mappedTransitions.keySet();
		}

		@Override
		public Collection<DFAStateWrapper<State>> getTransitions(char c) {
			DFAStateWrapper<State> result = mappedTransitions.get(c);
			if (result == null) {
				return Collections.emptySet();
			} else {
				return Collections.singleton(result);
			}
		}

		@Override
		public Collection<DFAStateWrapper<State>> getDefaultTransitions() {
			if (mappedTransitions.defaultReturnValue() == null) {
				return Collections.emptySet();
			} else {
				return Collections.singleton(mappedTransitions.defaultReturnValue());
			}
		}

		@Override
		public Collection<DFAStateWrapper<State>> getEpsilonTransitions() {
			return Collections.emptySet();
		}

	}

	private static class DFAConsumer<State> implements AutomatonConsumer<State> {

		private final DFA<State> dfa;
		private DFAStateWrapper<State> current;
		private final Deque<DFAStateWrapper<State>> parents; 

		public DFAConsumer(DFA<State> dfa) {
			this.dfa = dfa;
			current = dfa.initialState;
			parents = new ArrayDeque<DFA.DFAStateWrapper<State>>();
		}

		@Override
		public int consume(char c) {
			parents.addFirst(current);
			current = current.mappedTransitions.get(c);
			if (current == null) {
				current = parents.pollFirst();
				return 0;
			}
			return 1;
		}

		@Override
		public boolean cancel() {
			if (!parents.isEmpty()) {
				current = parents.pollFirst();
				return true;
			}
			return false;
		}

		@Override
		public Set<State> getCurrentStates() {
			if (current != null) {
				return Collections.singleton(current.state);
			}
			return Collections.emptySet();
		}

		@Override
		public boolean isAtFinalState() {
			return dfa.isFinal(current.state);
		}

		@Override
		public void reset() {
			parents.clear();
			current = dfa.getWrappedInitialState();
		}

	}

	private DFAStateWrapper<State> initialState;
	private Map<State, DFAStateWrapper<State>> states;

	public DFA(State initialState, CharComparator comparator) {
		super(comparator);
		this.states = new HashMap<State, DFAStateWrapper<State>>();
		this.initialState = getOrCreate(initialState);
	}

	public DFA(State initialState) {
		this(initialState, null);
	}

	private DFAStateWrapper<State> getOrCreate(State state) {
		DFAStateWrapper<State> result = states.get(state);
		if (result == null) {
			result = new DFAStateWrapper<State>(state, comparator);
			states.put(state, result);
		}
		return result;
	}

	@Override
	public DFAStateWrapper<State> getWrappedInitialState() {
		return initialState;
	}

	public void addTransition(State from, State to, char c) {
		getOrCreate(from).mappedTransitions.put(c, getOrCreate(to));
	}

	public void addDefaultTransition(State from, State to) {
		getOrCreate(from).mappedTransitions.defaultReturnValue(getOrCreate(to));
	}

	@Override
	public boolean accept(CharSequence seq, int offset, int length) {
		DFAStateWrapper<State> state = initialState;
		for (int i = 0; state != null && i < length; ++i) {
			char c = seq.charAt(offset + i);
			state = state.mappedTransitions.get(c);
		}
		if (state != null) {
			return isFinal(state.state);
		}
		return false;
	}

	@Override
	public boolean accept(char[] seq, int offset, int length) {
		DFAStateWrapper<State> state = initialState;
		for (int i = 0; initialState != null && i < length; ++i) {
			char c = seq[offset + i];
			state = state.mappedTransitions.get(c);
		}
		return isFinal(state.state);
	}

	@Override
	public Automaton.AutomatonConsumer<State> getConsumer() {
		return new DFA.DFAConsumer<State>(this);
	}

	protected Map<State, ? extends DFAStateWrapper<State>> getStates() {
		return states;
	}

	private static <State> void refinePartitions(
			Collection<Set<State>> partitions,
			Collection<Set<State>> queue,
			Collection<State> precedents) {

		Set<State> partition = null;
		Set<State> intersect = null;
		for (Set<State> p : partitions.toArray(new Set[partitions.size()])) {
			intersect = new HashSet<State>(precedents);
			intersect.retainAll(p);
			if (!intersect.isEmpty()) {
				partition = p;
				break;
			}
		}
		if (partition != null) {
			Set<State> difference = new HashSet<State>(partition);
			difference.removeAll(precedents);
			partitions.remove(partition);
			partitions.add(intersect);
			partitions.add(difference);
			if (queue.remove(partition)) {
				queue.add(intersect);
				queue.add(difference);
			} else if (intersect.size() <= difference.size()) {
				queue.add(intersect);
			} else {
				queue.add(difference);
			}
		}
	}

	public DFA<Set<State>> minimizeHopCroft() {
		NFA<State> reverse = reverse();

		Set<State> acceptingStates = new HashSet<State>(getFinalStates());
		Set<State> nonAcceptingStates = new HashSet<State>(getStates().keySet());
		nonAcceptingStates.removeAll(acceptingStates);

		@SuppressWarnings("unchecked")
		Collection<Set<State>> partitions = new HashSet<Set<State>>(Arrays.asList(acceptingStates, nonAcceptingStates));
		Set<Set<State>> queue = new HashSet<Set<State>>(Collections.singleton(acceptingStates));

		// Refine the partitions
		while (!queue.isEmpty()) {
			Iterator<Set<State>> iter = queue.iterator();
			Set<State> el = iter.next();
			iter.remove();

			for (State state : el) {

				for (CharIterator it = reverse.getMappedTransitions(state).iterator(); it.hasNext(); ) {
					char c = it.nextChar();
					Collection<State> precedent = reverse.getTransitions(state, c);
					refinePartitions(partitions, queue, precedent);
				}

				Collection<State> precedent = reverse.getDefaultTransitions(state);
				refinePartitions(partitions, queue, precedent);
			}
		}

		// Map from old to new states
		Map<State, Set<State>> mapping = new HashMap<State, Set<State>>();
		for (Set<State> partition : partitions) {
			for (State state : partition) {
				mapping.put(state, new ImmutableSet<State>(partition));
			}
		}

		// Add the transitions
		DFA<Set<State>> dfa = new DFA<Set<State>>(mapping.get(initialState.state));
		for (DFAStateWrapper<State> state : states.values()) {
			Set<State> from = mapping.get(state.state);
			if (isFinal(state.state)) {
				dfa.setFinal(from);
			}

			for (Map.Entry<Character, DFAStateWrapper<State>> entry : state.mappedTransitions.entrySet()) {
				char c = entry.getKey();
				DFAStateWrapper<State> to = entry.getValue();
				dfa.addTransition(from, mapping.get(to.state), c);
			}

			if (state.mappedTransitions.defaultReturnValue() != null) {
				DFAStateWrapper<State> to = state.mappedTransitions.defaultReturnValue();
				dfa.addDefaultTransition(from, mapping.get(to.state));
			}
		}

		return dfa;
	}

}
