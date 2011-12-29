package net.jpountz.charsequence;

/**
 * Common edit weights.
 */
public enum CommonEditWeight implements EditWeight, BasicEditWeight {
	INSERTION_DELETION {

		@Override
		public double substitutionCost() {
			return 2; // insertion + deletion
		}

		@Override
		public double transpositionCost() {
			return 2; // insertion + deletion
		}

	},
	LEVENSHTEIN {

		@Override
		public double substitutionCost() {
			return 1;
		}

		@Override
		public double transpositionCost() {
			return 2; // insertion + deletion or 2 substitutions
		}

	},
	DAMEREAU_LEVENSHTEIN {

		@Override
		public double substitutionCost() {
			return 1;
		}

		@Override
		public double transpositionCost() {
			return 1;
		}

	};

	boolean substitutionEnabled, transpositionEnabled;

	{
		substitutionEnabled = substitutionCost() < insertionCost() + deletionCost();
		transpositionEnabled = transpositionCost() < Math.min(
				insertionCost() + deletionCost(),
				2 * substitutionCost());
	}

	public double insertionCost() { return 1; }
	public double deletionCost()  { return 1; }
	public abstract double substitutionCost();
	public abstract double transpositionCost();

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
