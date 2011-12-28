package net.jpountz.charsequence;

public abstract class EditDistance extends CharSequenceDistance {

	/**
	 * Levenshtein distance.
	 * http://en.wikipedia.org/wiki/Levenshtein_distance
	 */
	public static final EditDistance LEVENSHTEIN_DISTANCE = new EditDistance() {

		@Override
		public double transpositionCost(
				int fromOffset, int toOffset, char c1, char c2) {
			return 2;
		}
		
		@Override
		public double substitutionCost(
				int fromOffset, int toOffset, char c1, char c2) {
			return 1;
		}

		@Override
		public double insertionCost(int offset, char c) {
			return 1;
		}

		@Override
		public double deletionCost(int offset, char c) {
			return 1;
		}
	};

	private static double min(double d1, double d2, double d3, double d4) {
		return Math.min(Math.min(Math.min(d1, d2), d3), d4);
	}

	@Override
	public double distance(
			CharSequence from, int off1, int len1,
			CharSequence to, int off2, int len2) {

		// Empty case
		if (len1 == 0) {
			if (len2 == 0) {
				return 0;
			}
			double result = 0;
			for (int i = 0; i < len2; ++i) {
				result += insertionCost(i, to.charAt(i));
			}
			return result;
		} else if (len2 == 0) {
			double result = 0;
			for (int i = 0; i < len1; ++i) {
				result += deletionCost(i, from.charAt(i));
			}
			return result;
		}


		double[] lastCosts = new double[len2+1];
		double[] costs = new double[len2+1];

		for (int i = 0; i <= len2; ++i) {
			lastCosts[i] = 0;
			for (int j = 0; j < i; ++j) {
				lastCosts[i] += insertionCost(j, to.charAt(j));
			}
		}

		for (int i = 1; i <= len1; ++i) {
			char cFrom = from.charAt(off1+i-1);
			costs[0] = lastCosts[0] + deletionCost(i-1, cFrom);

			for (int j = 1; j <= len2; ++j) {
				char cTo = to.charAt(off2+j-1);

				double insertionCost = costs[j-1] + insertionCost(j-1, cTo);
				double deletionCost = lastCosts[j] + deletionCost(i-1, cFrom);
				double substitutionCost = lastCosts[j-1];
				double transpositionCost = Double.MAX_VALUE;
				if (cFrom != cTo) {
					substitutionCost += substitutionCost(i-1, j-1, cFrom, cTo);
					if (i < len1 && j < len2) {
						char ccFrom = from.charAt(i);
						char ccTo = to.charAt(j);
						if (cFrom == ccTo && cTo == ccFrom) {
							transpositionCost = lastCosts[j-1] +
								transpositionCost(i-1, j-1, cFrom, cTo) -
								substitutionCost(i, j, ccTo, ccFrom);
						}
					}
				}

				costs[j] = min(insertionCost, deletionCost, substitutionCost, transpositionCost);
			}

			double[] tmp = lastCosts;
			lastCosts = costs;
			costs = tmp;
		}

		return lastCosts[len2];
	}

	/**
	 * c has been inserted in 'to' at offset
	 */
	public abstract double insertionCost(int offset, char c);

	/**
	 * c has been deleted from 'from' at offset
	 */
	public abstract double deletionCost(int offset, char c);

	/**
	 * c1 has been substituted to c2 from fromOffset to toOffsset
	 */
	public abstract double substitutionCost(int fromOffset, int toOffset, char c1, char c2);

	/**
	 * c1 has been substituted to c2 at offset and c2 has been substituted to c1 at offset+1
	 */
	public abstract double transpositionCost(int fromOffset, int toOffset, char c1, char c2);

}
