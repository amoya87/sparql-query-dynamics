package cl.uchile.dcc.dynamics.utils;

import java.util.Comparator;
import java.util.Map;

class ValueComparator <T> implements Comparator<T> {
	Map<T, Integer> base;

	public ValueComparator(Map<T, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	@Override
	public int compare(T a, T b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
