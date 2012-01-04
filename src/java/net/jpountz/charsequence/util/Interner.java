package net.jpountz.charsequence.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Intern values based on {@link Object#equals(Object)}.
 */
public class Interner<K> {

	private Map<K, K> references;

	public Interner() {
		this.references = new HashMap<K, K>();
	}

	public K intern(K value) {
		K result = references.get(value);
		if (result != null) {
			return result;
		} else {
			references.put(value, value);
			return value;
		}
	}

}
