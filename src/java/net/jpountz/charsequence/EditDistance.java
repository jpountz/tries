package net.jpountz.charsequence;

/**
 * Edit distance based on an {@link EditWeight} for various operations.
 */
public final class EditDistance extends CharSequenceDistance {

	public static final EditDistance INSERTION_DELETION = new EditDistance(CommonEditWeight.INSERTION_DELETION);
	public static final EditDistance LEVENSHTEIN = new EditDistance(CommonEditWeight.LEVENSHTEIN);
	public static final EditDistance DAMEREAU_LEVENSHTEIN = new EditDistance(CommonEditWeight.DAMEREAU_LEVENSHTEIN);

	private final EditWeight edit;

	public EditDistance(EditWeight edit) {
		this.edit = edit;
	}

	@Override
	public final double distance(
			CharSequence from, int off1, int len1,
			CharSequence to, int off2, int len2) {
		return distance(Double.MAX_VALUE,
				from, off1, len1,
				to, off2, len2);
	}

	public final double distance(double maxDistance, CharSequence from, CharSequence to) {
		return distance(maxDistance,
				from, 0, from.length(),
				to, 0, to.length());
	}

	public final double distance(double maxDistance,
			CharSequence from, int off1, int len1,
			CharSequence to, int off2, int len2) {

		// Empty case
		if (len1 == 0) {
			if (len2 == 0) {
				return 0;
			}
			double result = 0;
			for (int i = 0; i < len2; ++i) {
				result += edit.insertionCost(i, to.charAt(i));
			}
			return result;
		} else if (len2 == 0) {
			double result = 0;
			for (int i = 0; i < len1; ++i) {
				result += edit.deletionCost(i, from.charAt(i));
			}
			return result;
		}

		double[] lastCosts = new double[len2+1];
		double[] costs = new double[len2+1];

		for (int i = 0; i <= len2; ++i) {
			lastCosts[i] = 0;
			for (int j = 0; j < i; ++j) {
				lastCosts[i] += edit.insertionCost(j, to.charAt(j));
			}
		}

		for (int i = 1; i <= len1; ++i) {
			char cFrom = from.charAt(off1+i-1);
			costs[0] = lastCosts[0] + edit.deletionCost(i-1, cFrom);

			for (int j = 1; j <= len2; ++j) {
				char cTo = to.charAt(off2+j-1);

				double insertionCost = costs[j-1] + edit.insertionCost(j-1, cTo);
				double deletionCost = lastCosts[j] + edit.deletionCost(i-1, cFrom);

				double cost = Math.min(insertionCost, deletionCost);

				if (cFrom != cTo) {

					if (edit.substitutionEnabled()) {
						double substitutionCost = lastCosts[j-1] + edit.substitutionCost(i-1, j-1, cFrom, cTo);
						cost = Math.min(cost, substitutionCost);
					}

					if (edit.transpositionEnabled() && i < len1 && j < len2) {
						char ccFrom = from.charAt(i);
						char ccTo = to.charAt(j);
						if (cFrom == ccTo && cTo == ccFrom) {
							double transpositionCost = lastCosts[j-1] +
								edit.transpositionCost(i-1, j-1, cFrom, cTo) -
								edit.substitutionCost(i, j, ccTo, ccFrom);
							cost = Math.min(cost, transpositionCost);
						}
					}

				}

				else {
					cost = Math.min(cost, lastCosts[j-1]);
				}

				costs[j] = cost;
			}

			if (maxDistance < Double.MAX_VALUE) {
				boolean shouldContinue = false;
				for (double cost : costs) {
					if (cost <= maxDistance) {
						shouldContinue = true;
						break;
					}
				}
				if (!shouldContinue) {
					// max value exceeded
					return Double.MAX_VALUE;
				}
			}

			double[] tmp = lastCosts;
			lastCosts = costs;
			costs = tmp;
		}

		return lastCosts[len2];
	}

}
