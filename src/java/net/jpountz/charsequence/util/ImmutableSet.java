package net.jpountz.charsequence.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An immutable set.
 */
public class ImmutableSet<T> extends AbstractSet<T> {

	private final Set<T> elements;
	private final int hashCode;

	public ImmutableSet(Collection<T> elements) {
		if (elements.size() > 1 && !(elements instanceof Set)) {
			elements = new HashSet<T>(elements);
		}
		switch (elements.size()) {
		case 0:
			this.elements = Collections.emptySet();
			break;
		case 1:
			this.elements = Collections.singleton(elements.iterator().next());
			break;
		default:
			this.elements = Collections.unmodifiableSet(new HashSet<T>(elements));
			break;
		}
		this.hashCode = this.elements.hashCode();
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutableSet<?> other = (ImmutableSet<?>) obj;
		if (!elements.equals(other.elements))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return elements.toString();
	}
}
