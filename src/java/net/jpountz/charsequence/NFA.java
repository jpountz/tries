package net.jpountz.charsequence;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jpountz.charsequence.util.ImmutableSet;

/**
 * A non-deterministic finite-state automate.
 */
public class NFA<State> extends AbstractFA<State> {

	private static final int EPSILON = Character.MAX_VALUE + 1;
	private static final int ANY = Character.MAX_VALUE + 2;

	static class NFAStateWrapper<State> extends AbstractFA.StateWrapper<State> {

		final Char2ObjectMap<List<NFAStateWrapper<State>>> mappedTransitions;
		List<NFAStateWrapper<State>> defaultTransitions;
		List<NFAStateWrapper<State>> epsilonTransitions;

		public NFAStateWrapper(State state, CharComparator comparator) {
			super(state);
			this.mappedTransitions = new Char2ObjectRBTreeMap<List<NFAStateWrapper<State>>>(comparator);
			this.defaultTransitions = Collections.emptyList();
			this.epsilonTransitions = Collections.emptyList();
		}

		@Override
		public CharSet getMappedChars() {
			return mappedTransitions.keySet();
		}

		public List<NFAStateWrapper<State>> getTransitions(char c) {
			List<NFAStateWrapper<State>> result = mappedTransitions.get(c);
			if (result == null) {
				result = Collections.emptyList();
			}
			return result;
		}

		@Override
		public Collection<? extends net.jpountz.charsequence.AbstractFA.StateWrapper<State>> getDefaultTransitions() {
			return defaultTransitions;
		}

		@Override
		public Collection<? extends net.jpountz.charsequence.AbstractFA.StateWrapper<State>> getEpsilonTransitions() {
			return epsilonTransitions;
		}

		public void addTransition(char c, NFAStateWrapper<State> to) {
			List<NFAStateWrapper<State>> dests = mappedTransitions.get(c);
			if (dests == null) {
				dests = new ArrayList<NFA.NFAStateWrapper<State>>(1);
				mappedTransitions.put(c, dests);
			}
			dests.add(to);
		}

		public void addDefaultTransition(NFAStateWrapper<State> to) {
			if (defaultTransitions.isEmpty()) {
				defaultTransitions = new ArrayList<NFAStateWrapper<State>>(1);
			}
			defaultTransitions.add(to);
		}

		public void addEpsilonTransition(NFAStateWrapper<State> to) {
			if (epsilonTransitions.isEmpty()) {
				epsilonTransitions = new ArrayList<NFAStateWrapper<State>>(1);
			}
			epsilonTransitions.add(to);
		}

		private void getEpsilonReachableStates(Set<NFAStateWrapper<State>> result) {
			for (NFAStateWrapper<State> to : epsilonTransitions) {
				if (result.add(to)) {
					to.getEpsilonReachableStates(result);
				}
			}
		}

		@Override
		public void retainTransitions(
				Collection<? extends StateWrapper<State>> to) {
			for (Iterator<List<NFAStateWrapper<State>>> it = mappedTransitions.values().iterator(); it.hasNext(); ) {
				List<NFAStateWrapper<State>> next = it.next();
				next.retainAll(to);
				if (next.isEmpty()) {
					it.remove();
				}
			}
			defaultTransitions.retainAll(to);
			epsilonTransitions.retainAll(to);
		}

	}

	static class CursorState<State> {

		int[] consumed;
		NFAStateWrapper<State> state;

		CursorState(NFAStateWrapper<State> state, int[] consumed) {
			this.consumed = consumed;
			this.state = state;
		}

		@Override
		public int hashCode() {
			return state.hashCode() + 31 * Arrays.hashCode(consumed);
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder("[");
			if (consumed.length > 0) {
				for (int c : consumed) {
					switch (c) {
					case ANY:
						result.append("ANY, ");
						break;
					case EPSILON:
						result.append("EPSILON, ");
						break;
					default:
						result.append((char) c);
						result.append(", ");
						break;
					}
				}
				result.setLength(result.length() - 2);
			}
			result.append("] -> ");
			result.append(state.state.toString());
			return result.toString();
		}

	}

	static class NFAConsumer<State> implements AutomatonConsumer<State> {

		int consumed;
		private final NFA<State> automaton;
		private Set<CursorState<State>> visited; // after epsilon
		private final Deque<CursorState<State>> queue;
		private final Deque<Collection<CursorState<State>>> parents;
		private final Deque<Set<CursorState<State>>> previouslyVisited;

		public NFAConsumer(NFA<State> automaton) {
			consumed = 0;
			this.automaton = automaton;
			visited = new HashSet<CursorState<State>>();
			previouslyVisited = new ArrayDeque<Set<CursorState<State>>>();
			queue = new ArrayDeque<CursorState<State>>();
			parents = new ArrayDeque<Collection<CursorState<State>>>();
			reset();
		}

		private static int[] append(int[] array, int value) {
			int[] result = Arrays.copyOf(array, array.length + 1);
			result[result.length-1] = value;
			return result;
		}

		public int consume(char c) {
			parents.addFirst(new ArrayList<CursorState<State>>(queue));
			previouslyVisited.addFirst(new HashSet<CursorState<State>>(visited));

			++consumed;
			while (queue.peekFirst() != null && queue.peekFirst().consumed.length < consumed) {

				CursorState<State> cs = queue.pollFirst();
				visited.add(cs);
				int[] consumed = cs.consumed;

				for (NFAStateWrapper<State> state : cs.state.getTransitions(c)) {
					queue.addLast(new CursorState<State>(state, append(consumed, c)));
				}

				for (NFAStateWrapper<State> state : cs.state.defaultTransitions) {
					queue.addLast(new CursorState<State>(state, append(consumed, ANY)));
				}

				for (NFAStateWrapper<State> state : cs.state.epsilonTransitions) {
					CursorState<State> next;
					next = new CursorState<State>(state, append(consumed, ANY));
					if (!visited.add(next)) {
						queue.addFirst(next);
					}
				}

			}

			int result = queue.size();
			if (result == 0) {
				--consumed;
				queue.addAll(parents.pollFirst());
				visited.clear();
				visited = previouslyVisited.pollFirst();
			}
			return result;
		}

		@Override
		public Set<State> getCurrentStates() {
			Set<State> result = new HashSet<State>();
			for (CursorState<State> state : queue) {
				result.add(state.state.state);
			}
			return result;
		}

		public boolean isAtFinalState() {
			for (CursorState<State> cs : queue) {
				if (automaton.isFinal(cs.state.state)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean cancel() {
			if (consumed > 0) {
				visited.clear();
				visited.addAll(previouslyVisited.pollFirst());
				return true;
			}
			return false;
		}

		@Override
		public void reset() {
			queue.clear();
			parents.clear();
			visited.clear();
			previouslyVisited.clear();
			queue.push(new CursorState<State>(automaton.initialState, new int[0]));
		}

		@Override
		public String toString() {
			return queue.toString();
		}

	}

	private final NFAStateWrapper<State> initialState;
	private final Map<State, NFAStateWrapper<State>> states;

	public NFA(State initialState, CharComparator comparator) {
		super(comparator);
		this.states = new HashMap<State, NFAStateWrapper<State>>();
		this.initialState = getOrCreate(initialState);
	}

	public NFA(State initialState) {
		this(initialState, null);
	}

	private NFAStateWrapper<State> getOrCreate(State state) {
		NFAStateWrapper<State> result = states.get(state);
		if (result == null) {
			result = new NFAStateWrapper<State>(state, comparator);
			states.put(state, result);
		}
		return result;
	}

	public NFAStateWrapper<State> getWrappedInitialState() {
		return initialState;
	}

	public void addTransition(State from, State to, char c) {
		getOrCreate(from).addTransition(c, getOrCreate(to));
	}

	public void addDefaultTransition(State from, State to) {
		getOrCreate(from).addDefaultTransition(getOrCreate(to));
	}

	public void addEpsilonTransition(State from, State to) {
		getOrCreate(from).addEpsilonTransition(getOrCreate(to));
	}

	@Override
	public boolean accept(CharSequence seq, int offset, int length) {
		Deque<ConsumedState<NFAStateWrapper<State>>> stack = new ArrayDeque<ConsumedState<NFAStateWrapper<State>>>();
		stack.push(new ConsumedState<NFAStateWrapper<State>>(initialState,
				seq.toString().substring(offset, offset+length)));
		return accept(stack);
	}

	@Override
	public boolean accept(char[] seq, int offset, int length) {
		Deque<ConsumedState<NFAStateWrapper<State>>> stack = new ArrayDeque<ConsumedState<NFAStateWrapper<State>>>();
		stack.addFirst(new ConsumedState<NFAStateWrapper<State>>(initialState,
				new String(seq, offset, length)));
		return accept(stack);
	}

	private boolean accept(Deque<ConsumedState<NFAStateWrapper<State>>> stack) {
		Set<ConsumedState<NFAStateWrapper<State>>> visited = new HashSet<ConsumedState<NFAStateWrapper<State>>>();

		while (!stack.isEmpty()) {
			ConsumedState<NFAStateWrapper<State>> cs = stack.removeFirst();
			visited.add(cs);
			String remaining = cs.id;
			NFAStateWrapper<State> state = cs.state;

			if (remaining.isEmpty()) {
				if (isFinal(state.state)) {
					return true;
				}
			}

			else {
				List<NFAStateWrapper<State>> next = state.getTransitions(remaining.charAt(0));
				String suffix = remaining.substring(1);

				for (NFAStateWrapper<State> transition : next) {
					ConsumedState<NFAStateWrapper<State>> c = new ConsumedState<NFAStateWrapper<State>>(transition, suffix);
					if (visited.add(c)) {
						stack.addFirst(c);
					}
				}

				for (NFAStateWrapper<State> transition : state.defaultTransitions) {
					ConsumedState<NFAStateWrapper<State>> c = new ConsumedState<NFAStateWrapper<State>>(transition, suffix);
					if (visited.add(c)) {
						stack.addFirst(c);
					}
				}

			}

			for (NFAStateWrapper<State> transition : state.epsilonTransitions) {
				ConsumedState<NFAStateWrapper<State>> c = new ConsumedState<NFAStateWrapper<State>>(transition, remaining);
				if (visited.add(c)) {
					stack.addFirst(c);
				}
			}
		}		

		return false;
	}

	@Override
	public AutomatonConsumer<State> getConsumer() {
		return new NFA.NFAConsumer<State>(this);
	}

	@Override
	protected Map<State, ? extends net.jpountz.charsequence.AbstractFA.StateWrapper<State>> getStates() {
		return states;
	}

	private static <State> Set<State> toStateSet(Set<NFAStateWrapper<State>> states) {
		Set<State> result = new HashSet<State>();
		for (NFAStateWrapper<State> s : states) {
			result.add(s.state);
		}
		return new ImmutableSet<State>(result);
	}

	/**
	 * Determinize this automaton using powerset construction.
	 *
	 * @return
	 */
	public DFA<Set<State>> determinizePowerSet() {
		// get the ε-reachable states from the initial state
		Set<NFAStateWrapper<State>> initialStates = new HashSet<NFA.NFAStateWrapper<State>>();
		initialStates.add(initialState);
		initialState.getEpsilonReachableStates(initialStates);

		DFA<Set<State>> dfa = new DFA<Set<State>>(toStateSet(initialStates));

		Deque<Set<NFAStateWrapper<State>>> stack = new ArrayDeque<Set<NFAStateWrapper<State>>>();
		stack.add(initialStates);
		while (!stack.isEmpty()) {
			Set<NFAStateWrapper<State>> current = stack.pollFirst();
			Set<State> state = toStateSet(current);

			for (State s : state) {
				if (isFinal(s)) {
					dfa.addFinal(state);
					break;
				}
			}

			Set<NFAStateWrapper<State>> nextDefaultStates = new HashSet<NFAStateWrapper<State>>();
			for (NFAStateWrapper<State> s : current) {
				nextDefaultStates.addAll(s.defaultTransitions);
			}
			if (!nextDefaultStates.isEmpty()) {
				for (NFAStateWrapper<State> s : current) {
					for (NFAStateWrapper<State> transition : s.defaultTransitions) {
						transition.getEpsilonReachableStates(nextDefaultStates);
					}
					nextDefaultStates.addAll(s.defaultTransitions);
				}
				Set<State> next = toStateSet(nextDefaultStates);
				if (!dfa.hasState(next)) {
					stack.addFirst(nextDefaultStates);
				}
				dfa.addDefaultTransition(state, next);
			}

			CharSet chars = new CharOpenHashSet();
			for (NFAStateWrapper<State> s : current) {
				chars.addAll(s.mappedTransitions.keySet());
			}

			for (CharIterator it = chars.iterator(); it.hasNext(); ) {
				char c = it.nextChar();

				Set<NFAStateWrapper<State>> nextStates = new HashSet<NFAStateWrapper<State>>();
				for (NFAStateWrapper<State> s : current) {
					List<NFAStateWrapper<State>> transitions = s.getTransitions(c);
					for (NFAStateWrapper<State> transition : transitions) {
						transition.getEpsilonReachableStates(nextStates);
					}
					nextStates.addAll(transitions);
				}
				nextStates.addAll(nextDefaultStates);

				Set<State> next = toStateSet(nextStates);
				if (!dfa.hasState(next)) {
					stack.addFirst(nextStates);
				}
				dfa.addTransition(state, next, c);
			}
		}

		return dfa;
	}

}
