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

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jgrapht.ext.VertexNameProvider;

import soot.Unit;
import soot.tagkit.SourceLnPosTag;

/**
 * The Class VertexNameFilterProvider is a utility class used to name the
 * vertexes on a graph when transforming it to another serialized
 * representation, like a .DOT file.
 * 
 * @param <V>
 *            the value type
 */
public class VertexLineNameProvider<V extends Unit> implements VertexNameProvider<V> {

	/**
	 * Instantiates a new vertex name filter provider.
	 * 
	 * @param compilationUnit
	 *            the compilation unit
	 */
	public VertexLineNameProvider(CompilationUnit compilationUnit) {
		super();
	}

	/**
	 * Instantiates a new vertex name filter provider.
	 */
	@SuppressWarnings("unused")
	private VertexLineNameProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgrapht.ext.VertexNameProvider#getVertexName(java.lang.Object)
	 */
	@Override
	public String getVertexName(V vertex) {
		if (vertex.hasTag("SourceLnPosTag")) {
			SourceLnPosTag tag = (SourceLnPosTag) vertex.getTag("SourceLnPosTag");
			return tag.startLn()+"";
		}
		return "\"" + vertex.toString().replace("\"", "'") + "\"";
	}

}
