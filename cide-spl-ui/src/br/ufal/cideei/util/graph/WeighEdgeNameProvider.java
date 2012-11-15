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

package br.ufal.cideei.util.graph;

import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.EdgeNameProvider;

/**
 * The Class WeighEdgeNameProvider is a utility class used to provide a label
 * for edges when transforming it to a serializes format like .DOT. It simple
 * adds the double corresponding to its weight.
 * 
 * @param <E>
 *            the element type
 */
public class WeighEdgeNameProvider<E> implements EdgeNameProvider<E> {

	/** The graph. */
	private WeightedGraph<?, E> graph;

	/**
	 * Instantiates a new weigh edge name provider.
	 * 
	 * @param graph
	 *            the graph
	 */
	public WeighEdgeNameProvider(WeightedGraph<?, E> graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgrapht.ext.EdgeNameProvider#getEdgeName(java.lang.Object)
	 */
	@Override
	public String getEdgeName(E edge) {
		return graph.getEdgeWeight(edge) + "";
	}

}
