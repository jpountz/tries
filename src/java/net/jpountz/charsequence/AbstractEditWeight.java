package net.jpountz.charsequence;

/**
 * Base implementation for {@link EditWeight}s.
 */
public abstract class AbstractEditWeight implements EditWeight {

	@Override
	public double substitutionCost(
			int fromOffset, int toOffset,
			char c1, char c2) {
		return deletionCost(fromOffset, c1) + insertionCost(toOffset, c2);
	}

	@Override
	public double transpositionCost(
			int fromOffset, int toOffset,
			char c1, char c2) {
		return Math.min(Math.min(
				deletionCost(fromOffset, c1) + insertionCost(toOffset+1, c1),
				deletionCost(fromOffset+1, c2) + insertionCost(toOffset, c2)),
				substitutionCost(fromOffset, toOffset, c1, c2) +
				substitutionCost(fromOffset+1, toOffset+1, c2, c1));
	}

	@Override
	public boolean substitutionEnabled() {
		return true;
	}

	@Override
	public boolean transpositionEnabled() {
		return true;
	}

}
