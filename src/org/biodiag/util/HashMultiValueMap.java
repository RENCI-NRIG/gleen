package org.biodiag.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Peter Mork
 *
 * A HashMultiValueMap maps keys to sets of values.  (By comparison,
 * a HashMap maps each key to a single value.)
 */
public class HashMultiValueMap<A, B> {
	/**
	 * Maps keys to collections.
	 */
	private final HashMap<A, Set<B>> data = new HashMap<A, Set<B>>();
	/**
	 * Removes all elements from the map.
	 */
	public void clear() {
		data.clear();
	}
	/**
	 * Determines if the key is in the map.  It suffices to see if the underlying
	 * HashMap contains the key.
	 */
	public boolean containsKey(A key) {
		return data.containsKey(key);
	}
	/**
	 * Determines if the value is in the map.  Since each value in the underlying
	 * HashMap is a collection, we must search each.
	 */
	public boolean containsValue(B value) {
		for (Iterator<Set<B>> valueItr = data.values().iterator(); valueItr.hasNext();) {
			Set<B> values = valueItr.next();
			if (values.contains(value))
				return true;
		}
		return false;
	}
	/**
	 * @return A set of mappings from keys to sets.
	 */
	public Set<Map.Entry<A,Set<B>>> entrySet() {
		return data.entrySet();
	}
	/**
	 * Tests equality.
	 */
	public boolean equals(Object o) {
		return data.equals(o);
	}
	/**
	 * @return A set of values associated with this key.  An empty set is
	 * returned when there are no values.  Null will never be returned.
	 */
	public Set<B> get(A key) {
		Set<B> result = data.get(key);
		if (result == null)
			result = new HashSet<B>();
		return result;
	}
	/**
	 * @return A single value associated with the key.
	 */
	public B getValue(A key) {
		B result = null;
		Set<B> values = get(key);
		if (!values.isEmpty()) {
			result = values.iterator().next();
		}
		return result;
	}
	/**
	 * @return The underlying map's hash code.
	 */
	public int hashCode() {
		return data.hashCode();
	}
	/**
	 * @return <code>True</code> iff the map has no keys.
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}
	/**
	 * @return The underlyings map's set of keys.
	 */
	public Set<A> keySet() {
		return data.keySet();
	}
	/**
	 * Adds a value to the map.  If the key is already in use, the associated
	 * collection is expanded, otherwise a new collection is used.  The modified
	 * collection is returned.
	 */
	public Set<B> put(A key, B value) {
		if(this.containsKey(key)) {
			Set<B> result = this.get(key);
			result.add(value);
			return result;
		} else {
			Set<B> result = new HashSet<B>();
			result.add(value);
			data.put(key, result);
			return result;
		}
	}
	/**
	 * Adds all of the mappings in some other map.  Note: If the associated value
	 * is a collection, that collection is flattened and treated like a multi-
	 * valued set.
	 */
	public void putAll(Map<A, B> t) {
		for (Iterator<Map.Entry<A,B>> entryItr = t.entrySet().iterator(); entryItr.hasNext();) {
			Map.Entry<A,B> entry = entryItr.next();
			put(entry.getKey(), entry.getValue());
		}
	}
	/**
	 * Removes a key (and returns the associated set of values).
	 */
	public Set<B> remove(A key) {
		return data.remove(key);
	}
	/**
	 * Removes a key/value pair.  The key is removed from the underlying map
	 * if this was the last value.
	 */
	public void removeValue(A key, B value) {
		Set<B> values = get(key);
		values.remove(value);
		if (values.isEmpty())
			remove(key);
	}
	/**
	 * @return The number of keys in the underlying map.
	 */
	public int size() {
		return data.size();
	}
	/**
	 * Flattens the associated sets of values into one monster set.
	 */
	public Collection<B> values() {
		Set<B> result = new HashSet<B>();
		for (Iterator<Set<B>> valueItr = data.values().iterator(); valueItr.hasNext();) {
			Set<B> values = valueItr.next();
			result.addAll(values);
		}
		return result;
	}
	
	
	public String toString() {
		return data.toString();
	}
}