package net.jpountz.charsequence;

/**
 * Comparator of characters.
 */
public interface CharComparator {

	/**
	 * Default {@link CharComparator}. Returns:
	 *   compare(c1, c2) == 0 only if, and only if c1 == c2,
	 *   compare(c1, c2) < 0 if, and only if c1 < c2.
	 */
	public static final CharComparator DEFAULT = new CharComparator() {

		public int compare(char c1, char c2) {
			return (int) c1 - c2;
		}

	};

	int compare(char c1, char c2);

}
