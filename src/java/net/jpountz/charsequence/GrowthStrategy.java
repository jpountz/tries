package net.jpountz.charsequence;

/**
 * Class that tells how to grow to an array-based object.
 */
public interface GrowthStrategy {

	/**
	 * Returns the next capacity that exceeds minimum.
	 *
	 * @param minimum
	 * @return the next capacity > minimum
	 */
	int grow(int minimum);

	/**
	 * A {@link GrowthStrategy} which ensures fast growth in order to minimize
	 * the resizing overhead.
	 */
	public static final GrowthStrategy FAST_GROWTH = new GrowthStrategy() {

		public int grow(int minimum) {
			if (minimum <= 0) {
				return 1;
			}
			return 2 * minimum;
		}

	};

	/**
	 * A {@link GrowthStrategy} which favors memory-efficiency.
	 */
	public static final GrowthStrategy AMORTIZED_GROWTH = new GrowthStrategy() {

		private static final int MAX_BYTES_PER_ELEMENT = 8;
		private static final int MASK = 0x7ffffff8;

		public int grow(int minimum) {
			if (minimum < MAX_BYTES_PER_ELEMENT) {
				return MAX_BYTES_PER_ELEMENT;
			} else {
				int result = minimum;
				int extra = Math.max(MAX_BYTES_PER_ELEMENT, result >>>3);
				return (result + extra) & MASK;
			}
		}

	};

}
