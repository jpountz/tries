package net.jpountz.charsequence;

/**
 * Weight of the different edits which can be made to a {@link CharSequence}.
 * Don't implement directly, use {@link AbstractEditWeight}.
 *
 * To have consistent results, an {@link EditWeight} should obey the following
 * rules:
 *   max(substitutionCost)  <= min(insertionCost) + min(deletionCost)
 *   max(transpositionCost) <= 2*substitutionCost
 *                          <= min(insertionCost) + min(deletionCost)
 */
public interface EditWeight {

	/**
	 * c has been inserted in 'to' at offset
	 */
	double insertionCost(int offset, char c);

	/**
	 * c has been deleted from 'from' at offset
	 */
	double deletionCost(int offset, char c);

	/**
	 * c1 has been substituted to c2 from fromOffset to toOffsset
	 */
	double substitutionCost(int fromOffset, int toOffset, char c1, char c2);

	/**
	 * c1 has been substituted to c2 at offset and c2 has been substituted to c1 at offset+1
	 */
	double transpositionCost(int fromOffset, int toOffset, char c1, char c2);

	/**
	 * This method is used by some algorithms to perform optimisations. It
	 * should only return false if the substitutions are guaranteed to be
	 * cheaper than any combination of insertions and deletions.
	 */
	boolean substitutionEnabled();

	/**
	 * This method is used by some algorithms to perform optimisations. It
	 * should only return false if the transpositions are guaranteed to be
	 * cheaper than any combination of insertions, deletions and substitutions.
	 */
	boolean transpositionEnabled();

}
