package net.jpountz.charsequence;

import java.util.ArrayDeque;
import java.util.Deque;

public enum Automata {
	;

	public static final class DistanceState {

		private final String remainingChars;
		private final double remainingDistance;

		public DistanceState(String remainingChars, double remainingDistance) {
			this.remainingChars = remainingChars;
			this.remainingDistance = remainingDistance;
		}

		public String getRemainingChars() {
			return remainingChars;
		}

		public double getRemainingDistance() {
			return remainingDistance;
		}

		@Override
		public int hashCode() {
			long temp = Double.doubleToLongBits(remainingDistance);
			return remainingChars.hashCode() ^ (int) (temp ^ (temp >>> 32));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DistanceState other = (DistanceState) obj;
			if (!remainingChars.equals(other.remainingChars))
				return false;
			if (Double.doubleToLongBits(remainingDistance) != Double
					.doubleToLongBits(other.remainingDistance))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return remainingChars + '(' + remainingDistance + ')';
		}

	}

	public static DFA<Integer> forWord(CharSequence word) {
		Integer previous = 0;
		DFA<Integer> result = new DFA<Integer>(previous);
		for (int i = 0; i < word.length(); ++i) {
			Integer current = i+1;
			result.addTransition(
					previous,
					current,
					word.charAt(i));
			previous = current;
		}
		result.addFinal(previous);
		return result;
	}

	public static NFA<DistanceState> forEditWeight(
			CharSequence seq, BasicEditWeight edit, double maxDistance) {
		DistanceState initialState = new DistanceState(seq.toString(), maxDistance);
		NFA<DistanceState> automaton = new NFA<DistanceState>(initialState);

		Deque<DistanceState> states = new ArrayDeque<DistanceState>();
		states.addFirst(initialState);

		DistanceState finalState = new DistanceState("", 0);
		automaton.addFinal(finalState);

		while (states.peekFirst() != null) {
			DistanceState state = states.pollFirst();
			String remainingChars = state.remainingChars;
			double remainingDistance = state.remainingDistance;

			if (!remainingChars.isEmpty()) {

				String suffix = remainingChars.substring(1);

				// match
				DistanceState next = new DistanceState(suffix, remainingDistance);
				if (!automaton.hasState(next)) {
					states.addFirst(next);
				}
				automaton.addTransition(state, next, remainingChars.charAt(0));

				// deletion
				double deletionDistance = remainingDistance - edit.deletionCost();
				if (deletionDistance >= 0) {
					next = new DistanceState(suffix, deletionDistance);
					if (!automaton.hasState(next)) {
						states.addFirst(next);
					}
					automaton.addEpsilonTransition(state, next);
				}

				// substitution
				if (edit.substitutionEnabled()) {
					double substitutionDistance = remainingDistance - edit.substitutionCost();
					if (substitutionDistance >= 0) {
						next = new DistanceState(suffix, substitutionDistance);
						if (!automaton.hasState(next)) {
							states.addFirst(next);
						}
						automaton.addDefaultTransition(state, next);
					}
				}
			}

			// transposition
			if (edit.transpositionEnabled() && remainingChars.length() >= 2 &&
					remainingChars.charAt(0) != remainingChars.charAt(1)) {
				double transpositionDistance = remainingDistance - edit.transpositionCost();
				if (transpositionDistance >= 0) {
					String suffix = remainingChars.substring(2);
					DistanceState next = new DistanceState(remainingChars.charAt(0) + suffix, transpositionDistance);
					DistanceState nnext = new DistanceState(suffix, transpositionDistance);
					if (!automaton.hasState(nnext)) {
						states.addFirst(nnext);
					}
					automaton.addTransition(state, next, remainingChars.charAt(1));
					automaton.addTransition(next, nnext, remainingChars.charAt(0));
				}
			}

			// insertion
			double insertionDistance = remainingDistance - edit.insertionCost();
			if (insertionDistance >= 0) {
				DistanceState next = new DistanceState(remainingChars, insertionDistance);
				if (!automaton.hasState(next)) {
					states.addFirst(next);
				}
				automaton.addDefaultTransition(state, next);
			}

			if (remainingChars.length() * edit.deletionCost() <= remainingDistance) {
				if (!state.equals(finalState)) {
					automaton.addEpsilonTransition(state, finalState);
				}
			}
		}

		return automaton;
	}

}
