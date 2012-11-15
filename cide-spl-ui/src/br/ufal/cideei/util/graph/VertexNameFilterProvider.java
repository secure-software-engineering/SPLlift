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

import org.jgrapht.ext.VertexNameProvider;

import soot.Unit;
import soot.tagkit.SourceLnPosTag;
import br.ufal.cideei.soot.instrument.FeatureTag;

/**
 * The Class VertexNameFilterProvider is a utility class used to name the
 * vertexes on a graph when transforming it to another serializes
 * representation, like a .DOT file.
 * 
 * @param <V>
 *            the value type
 */
public class VertexNameFilterProvider<V extends Unit> implements VertexNameProvider<V> {

	/**
	 * Instantiates a new vertex name filter provider.
	 */
	public VertexNameFilterProvider() {
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
			String feat;
			try {
				FeatureTag ftag = (FeatureTag) vertex.getTag(FeatureTag.FEAT_TAG_NAME);
				feat = ftag.getClass().toString();
			} catch (Exception ex) {
				ex.printStackTrace();
				feat = "";
			}

			// String feat = "";
			return "\"" + "(" + tag.startLn() + ")" + vertex.toString().replace("\"", "'") + feat + "\"";
		}
		return "\"" + vertex.toString().replace("\"", "'") + "\"";
	}

}
