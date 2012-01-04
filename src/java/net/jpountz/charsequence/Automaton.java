package net.jpountz.charsequence;

import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.chars.CharSets;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An automaton.
 */
public abstract class Automaton<State> extends AbstractAccepter {

	private static class StateCollection<State> extends AbstractCollection<State> {

		private final Collection<? extends StateWrapper<State>> states;

		public StateCollection(Collection<? extends StateWrapper<State>> states) {
			this.states = states;
		}

		@Override
		public Iterator<State> iterator() {
			return new Iterator<State>() {

				private final Iterator<? extends StateWrapper<State>> it;
				{
					it = states.iterator();
				}

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public State next() {
					StateWrapper<State> next = it.next();
					if (next != null) {
						return next.state;
					}
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

		@Override
		public int size() {
			return states.size();
		}
		
	}

	public static interface AutomatonConsumer<State> {

		/**
		 * Consume a char and return the number of current paths.
		 * In case 0 is returned, the state of the consumer hasn't changed.
		 *
		 * @param c the char to consume
		 * @return the number of current paths
		 */
		int consume(char c);

		/**
		 * Cancel the last character consumption.
		 */
		boolean cancel();

		/**
		 * Get the current states.
		 *
		 * @param states a collection to fill
		 */
		Set<State> getCurrentStates();

		/**
		 * Whether one of the current paths is at a final state.
		 *
		 * @return whether the consumer is in a final position
		 */
		boolean isAtFinalState();

		/**
		 * Reset this consumer.
		 */
		void reset();

	}

	protected abstract static class StateWrapper<State> {

		protected final State state;

		public StateWrapper(State state) {
			this.state = state;
		}

		public abstract CharSet getMappedChars();

		public abstract Collection<? extends StateWrapper<State>> getTransitions(char c);

		public abstract Collection<? extends StateWrapper<State>> getDefaultTransitions();

		public abstract Collection<? extends StateWrapper<State>> getEpsilonTransitions();

		@Override
		public final int hashCode() {
			return state == null ? 0 : state.hashCode();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final boolean equals(Object other) {
			if (other instanceof StateWrapper && other != null) {
				State otherState = ((StateWrapper<State>) other).state;
				if (state == null) {
					return otherState == null;
				} else {
					return state.equals(otherState);
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return toString(Collections.<State>emptySet());
		}

		public String toString(Set<State> finalStates) {
			StringBuilder result = new StringBuilder();
			result.append(state);
			if (finalStates.contains(state)) {
				result.append(" (final)");
			}
			result.append(": [");

			boolean oneTransition = false;

			for (CharIterator it = getMappedChars().iterator(); it.hasNext(); ) {
				char c = it.nextChar();

				for (StateWrapper<State> s : getTransitions(c)) {
					result.append('\'').append(c).append('\'');
					result.append(" -> ");
					result.append(s.state);
					result.append(", ");
				}
				oneTransition = true;
			}
			if (!getDefaultTransitions().isEmpty()) {
				for (StateWrapper<State> s : getDefaultTransitions()) {
					result.append("* -> ");
					result.append(s.state);
					result.append(", ");
				}
				oneTransition = true;
			}
			if (!getEpsilonTransitions().isEmpty()) {
				for (StateWrapper<State> s : getEpsilonTransitions()) {
					result.append("ε -> ");
					result.append(s.state);
					result.append(", ");
				}
				oneTransition = true;
			}
			if (oneTransition) {
				result.setLength(result.length() - 2);
			}
			result.append(']');
			return result.toString();
		}

	}

	protected static class ConsumedState<State> {

		State state;
		String id;

		public ConsumedState(State state, String remaining) {
			this.state = state;
			this.id = remaining;
		}

		@Override
		public int hashCode() {
			return state.hashCode() + 31 * id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("unchecked")
			ConsumedState<Object> other = (ConsumedState<Object>) obj;
			if (!id.equals(other.id))
				return false;
			if (!state.equals(other.state))
				return false;
			return true;
		}

	}

	protected static class AutomatonIterator<State> implements Iterator<String> {

		private String next;
		private final Automaton<State> automaton;
		private final CharSet alphabet;
		private final Deque<ConsumedState<StateWrapper<State>>> stack;
		private final Set<ConsumedState<StateWrapper<State>>> seenStates;
		private final Set<String> seenWords;

		public AutomatonIterator(Automaton<State> automaton, CharSet alphabet) {
			this.automaton = automaton;
			this.alphabet = alphabet;
			this.stack = new ArrayDeque<ConsumedState<StateWrapper<State>>>();
			this.stack.addFirst(new ConsumedState<StateWrapper<State>>(
					automaton.getWrappedInitialState(), ""));
			this.seenStates = new HashSet<ConsumedState<StateWrapper<State>>>();
			this.seenWords = new HashSet<String>();
		}

		@Override
		public boolean hasNext() {
			return next != null || (next = next()) != null;
		}

		@Override
		public String next() {
			if (next != null) {
				String result = next;
				next = null;
				return result;
			}

			while (stack.peekFirst() != null) {

				ConsumedState<StateWrapper<State>> cs = stack.pollFirst();

				if (!seenStates.add(cs)) continue;

				for (char c : alphabet) {
					String nextId = cs.id + c;
					for (StateWrapper<State> state : cs.state.getTransitions(c)) {
						ConsumedState<StateWrapper<State>> next = new ConsumedState<StateWrapper<State>>(state, nextId);
						if (!seenStates.contains(next)) {
							stack.addFirst(next);
						}
					}
					for (StateWrapper<State> state : cs.state.getDefaultTransitions()) {
						ConsumedState<StateWrapper<State>> next = new ConsumedState<StateWrapper<State>>(state, nextId);
						if (!seenStates.contains(next)) {
							stack.addFirst(next);
						}
					}
				}

				for (StateWrapper<State> state : cs.state.getEpsilonTransitions()) {
					ConsumedState<StateWrapper<State>> next = new ConsumedState<StateWrapper<State>>(state, cs.id);
					if (!seenStates.contains(next)) {
						stack.addLast(next);
					}
				}

				if (automaton.isFinal(cs.state.state) && !seenWords.contains(cs.id)) {
					seenWords.add(cs.id);
					return cs.id;
				}
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	protected Set<State> finalStates;

	protected Automaton() {
		this.finalStates = new HashSet<State>();
	}

	public abstract StateWrapper<State> getWrappedInitialState();

	public State getInitialState() {
		return getWrappedInitialState().state;
	}

	public Set<State> getFinalStates() {
		return Collections.unmodifiableSet(finalStates);
	}

	public boolean isFinal(State state) {
		return finalStates.contains(state);
	}

	public void setFinal(State state) {
		finalStates.add(state);
	}

	public void unsetFinal(State state) {
		finalStates.remove(state);
	}

	public abstract void addTransition(State from, State to, char c);

	public abstract void addDefaultTransition(State from, State to);

	public void addEpsilonTransition(State from, State to) {
		throw new UnsupportedOperationException();
	}

	public abstract AutomatonConsumer<State> getConsumer();

	public Iterable<String> getDictionary(final CharSet alphabet) {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return new AutomatonIterator<State>(Automaton.this, alphabet);
			}

		};
	}

	protected abstract Map<State, ? extends StateWrapper<State>> getStates();

	public boolean hasState(State state) {
		return getStates().containsKey(state);
	}

	public int getNumberOfStates() {
		return getStates().size();
	}

	public CharSet getMappedTransitions(State from) {
		StateWrapper<State> s = getStates().get(from);
		if (s != null) {
			return CharSets.unmodifiable(s.getMappedChars());
		}
		return CharSets.EMPTY_SET;
	}

	public Collection<State> getTransitions(State from, char c) {
		StateWrapper<State> s = getStates().get(from);
		if (s != null) {
			return new StateCollection<State>(s.getTransitions(c));
		}
		return Collections.emptySet();
	}

	public Collection<State> getDefaultTransitions(State from) {
		StateWrapper<State> s = getStates().get(from);
		if (s != null) {
			return new StateCollection<State>(s.getDefaultTransitions());
		}
		return Collections.emptySet();
	}

	public Collection<State> getEpsilonTransitions(State from) {
		StateWrapper<State> s = getStates().get(from);
		if (s != null) {
			return new StateCollection<State>(s.getEpsilonTransitions());
		}
		return Collections.emptySet();
	}

	public NFA<State> reverse() {
		if (finalStates.size() != 1) {
			throw new IllegalStateException("Can only reverse automata with exactly one final state");
		}
		NFA<State> reverse = new NFA<State>(finalStates.iterator().next());

		for (StateWrapper<State> to : getStates().values()) {
			for (CharIterator it = to.getMappedChars().iterator(); it.hasNext(); ) {
				char c = it.nextChar();

				for (StateWrapper<State> from : to.getTransitions(c)) {
					reverse.addTransition(from.state, to.state, c);
				}
			}

			for (StateWrapper<State> from : to.getDefaultTransitions()) {
				reverse.addDefaultTransition(from.state, to.state);
			}

			for (StateWrapper<State> from : to.getEpsilonTransitions()) {
				reverse.addEpsilonTransition(from.state, to.state);
			}
		}

		reverse.setFinal(getInitialState());

		return reverse;
	}

	public DFA<Set<Set<State>>> minimizeBrzozowski() {
		return reverse().determinizePowerSet().reverse().determinizePowerSet();
	}

	@Override
	public String toString() {
		Collection<? extends StateWrapper<State>> states = getStates().values();
		if (states.isEmpty()) {
			return "()";
		}
		StringBuilder result = new StringBuilder("(");
		result.append(getWrappedInitialState().toString(finalStates));
		result.append(", ");
		for (StateWrapper<State> s : states) {
			if (getWrappedInitialState().equals(s)) {
				continue;
			}
			result.append(s.toString(finalStates));
			result.append(", ");
		}
		result.setLength(result.length() - 2);
		result.append(')');
		return result.toString();
	}

}
