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
import java.util.Formatter;
import java.util.Locale;

public class ExecutionResultWrapper<T extends Number> {
	private ArrayList<T> al;

	public ExecutionResultWrapper(int size) {
		al = new ArrayList<T>(size);
	}

	public ExecutionResultWrapper() {
		al = new ArrayList<T>();
	}

	public void add(T obj) {
		al.add(obj);
	}

	public double mean() {
		Double sum = new Double(0);
		for (T t : al) {
			sum += t.doubleValue();
		}
		return sum / al.size();
	}
	
	public T get(int i) {
		return al.get(i);
	}

	public String toString() {
		int size = al.size();
		StringBuilder builder;
		if (size == 0) {
			builder = new StringBuilder(0);
		} else {
			builder = new StringBuilder(size + size - 1);
		}
		builder.append("[");
		for (T t : al) {
			Formatter formatter = new Formatter();
			builder.append(formatter.format(Locale.FRANCE, "%10.4f", t).out());
			builder.append("\t");
		}
		builder.append("]");
		return builder.toString();
	}
}
