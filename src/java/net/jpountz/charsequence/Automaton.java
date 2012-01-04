package net.jpountz.charsequence;

import it.unimi.dsi.fastutil.chars.CharSet;

import java.util.Set;

/**
 * An automaton.
 */
public interface Automaton<State> extends Accepter {

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

	/**
	 * Get the initial state of this automaton.
	 *
	 * @return the initial state
	 */
	State getInitialState();

	/**
	 * Get a consumer for this automaton.
	 *
	 * @return
	 */
	AutomatonConsumer<State> getConsumer();

	/**
	 * Generate the dictionary for this automaton.
	 *
	 * @param alphabet the alphabet to use
	 * @return the dictionary
	 */
	Iterable<String> getDictionary(final CharSet alphabet);

	/**
	 * Whether this automaton contains the given state.
	 *
	 * @param state
	 * @return
	 */
	boolean hasState(State state);

	/**
	 * Get the number of states of this automaton, negative if this automaton
	 * has an infinite number of states.
	 */
	int getNumberOfStates();

}
