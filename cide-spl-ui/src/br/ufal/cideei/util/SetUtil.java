/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.ovgu.cide.features.IFeature;

// TODO: Auto-generated Javadoc
/**
 * The Class SetUtil is a utility class to generate relevant sets to the
 * application.
 */
public class SetUtil {

	/**
	 * Instantiates a new sets the util.
	 */
	private SetUtil() {
	}

	/**
	 * Recursively generates a power set for a given set.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param originalSet
	 *            the original set
	 * @return the sets the
	 */
	// TODO: look for a more efficient implementation.
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	/**
	 * Generates a valid configuration sets for given set of features and a
	 * power set.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param featurePowerSet
	 *            the feature power set
	 * @param featuresInUaf
	 *            the features in uaf
	 * @return the sets the
	 */
	public static <T> Set<Set<T>> configurationSet(Set<Set<T>> featurePowerSet, Set<IFeature> featuresInUaf) {
		Iterator<Set<T>> powerSetIterator = featurePowerSet.iterator();
		Set<Set<T>> resultingSet = new HashSet<Set<T>>();
		while (powerSetIterator.hasNext()) {
			Set<T> nextSubSet = (Set<T>) powerSetIterator.next();
			boolean foundFeature = false;

			if (featuresInUaf.size() >= 1) {
				Iterator<IFeature> iterator = featuresInUaf.iterator();
				while (iterator.hasNext()) {
					IFeature feature = iterator.next();
					if (nextSubSet.contains(feature.getName())) {
						foundFeature = true;
					} else {
						foundFeature = false;
						break;
					}
				}
			}
			if (foundFeature) {
				resultingSet.add(nextSubSet);
			}
		}
		return resultingSet;
	}

	/**
	 * Returns the power set from the given set by using a binary counter
	 * Example: S = {a,b,c}
	 * 
	 * P(S) = {[], [c], [b], [b, c], [a], [a, c], [a, b], [a, b, c]}
	 * 
	 * @param set
	 *            String[]
	 * @return LinkedHashSet
	 */
	// TODO: retained as legacy. Search for a more efficient implementation.
	private static Set<Set<String>> powerset(String[] set) {

		// create the empty power set
		LinkedHashSet<Set<String>> power = new LinkedHashSet<Set<String>>();

		// get the number of elements in the set
		int elements = set.length;

		// the number of members of a power set is 2^n
		int powerElements = (int) Math.pow(2, elements);

		// run a binary counter for the number of power elements
		for (int i = 0; i < powerElements; i++) {

			// convert the binary number to a string containing n digits
			String binary = intToBinary(i, elements);

			// create a new set
			LinkedHashSet<String> innerSet = new LinkedHashSet<String>();

			// convert each digit in the current binary number to the
			// corresponding element
			// in the given set
			for (int j = 0; j < binary.length(); j++) {
				if (binary.charAt(j) == '1')
					innerSet.add(set[j]);
			}

			// add the new set to the power set
			power.add(innerSet);

		}

		return power;
	}

	/**
	 * Converts the given integer to a String representing a binary number with
	 * the specified number of digits For example when using 4 digits the binary
	 * 1 is 0001
	 * 
	 * @param binary
	 *            int
	 * @param digits
	 *            int
	 * @return String
	 */
	private static String intToBinary(int binary, int digits) {

		String temp = Integer.toBinaryString(binary);
		int foundDigits = temp.length();
		String returner = temp;
		for (int i = foundDigits; i < digits; i++) {
			returner = "0" + returner;
		}

		return returner;
	}

}
