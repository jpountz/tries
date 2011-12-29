package net.jpountz.charsequence;

/**
 * Basic implementation of {@link EditWeight}.
 */
public abstract class AbstractBasicEditWeight extends AbstractEditWeight implements BasicEditWeight {

	private final boolean substitutionEnabled, transpositionEnabled;

	public AbstractBasicEditWeight() {
		substitutionEnabled = substitutionCost() < insertionCost() + deletionCost();
		transpositionEnabled = transpositionCost() < Math.min(
				insertionCost() + deletionCost(),
				2 * substitutionCost());
	}

	public abstract double insertionCost();
	public abstract double deletionCost();
	public double substitutionCost() {
		return insertionCost() + deletionCost();
	}
	public double transpositionCost() {
		return Math.min(
				insertionCost() + deletionCost(),
				2 * substitutionCost());
	}

	@Override
	public double insertionCost(int offset, char c) {
		return insertionCost();
	}

	@Override
	public double deletionCost(int offset, char c) {
		return deletionCost();
	}

	@Override
	public double substitutionCost(
			int fromOffset, int toOffset, char c1, char c2) {
		return substitutionCost();
	}

	@Override
	public double transpositionCost(
			int fromOffset, int toOffset, char c1, char c2) {
		return transpositionCost();
	}

	public boolean substitutionEnabled() {
		return substitutionEnabled;
	}

	public boolean transpositionEnabled() {
		return transpositionEnabled;
	}

}
