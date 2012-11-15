package soot.spl.ifds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Contains functions to lazily compute the power set of some set. 
 */
public class Powerset {

	public static <T> Iterable<Set<T>> powerset(final Set<T> set) {
		return powerset(set, true);
	}

	public static <T> Iterable<Set<T>> powerset(final Set<T> set, final boolean includeEmptySet) {
        return new Iterable<Set<T>>() {
        	final Iterator<Set<T>> ITER = new PowerSetIterator<T>(set, includeEmptySet);
        	
			@Override
			public Iterator<Set<T>> iterator() {
				return ITER;
			}
		};
	}

	private static class PowerSetIterator<T> implements Iterator<Set<T>> {

		private final List<T> list;
		private final int powerTwo;
		private int cursor;

		private PowerSetIterator(Set<T> set, boolean includeEmptySet) {
			cursor = includeEmptySet ? 0 : 1;
			list = new ArrayList<T>(set);	
			powerTwo = (int) Math.pow(2,set.size());
		}
		
		@Override
		public boolean hasNext() {
			return cursor < powerTwo;
		}

		@Override
		public Set<T> next() {
        	Set<T> set = new HashSet<T>();
	         
        	for (int i = 0; i < list.size(); i++) {
        		if ((cursor>>i & 1)!=0)
        			set.add(list.get(i));
        	}
        	
        	cursor++;
        	
        	return set;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove elements using this iterator.");
		}
		
	}

//	public static void main(String[] args) {
//		for(Set<K> set: powerset(new HashSet<K>(Arrays.asList(K.values())),false)) {
//			System.err.println(set);
//		}
//	}
//	
//	enum K {A,B,C}
}
