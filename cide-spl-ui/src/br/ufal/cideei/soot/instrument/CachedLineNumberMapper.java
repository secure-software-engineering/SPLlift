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

package br.ufal.cideei.soot.instrument;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import br.ufal.cideei.features.IFeatureExtracter;

public class CachedLineNumberMapper {
	private LineNumberColorMapper colorMapper = null;

	// Caching key
	private ASTNode visitee = null;

	// Cached target
	private Map<Integer, Set<String>> cachedResult = null;

	public Map<Integer, Set<String>> makeAccept(CompilationUnit compilationUnit, IFile file, IFeatureExtracter extracter, ASTNode node) {
		// itialized and cached: HIT
		if (visitee != null && node.equals(visitee)) {
			return cachedResult;
		} else {
			//MISS
			colorMapper = new LineNumberColorMapper(compilationUnit, file, extracter);
			visitee = node;
			visitee.accept(colorMapper);
			return this.cachedResult = colorMapper.getLineToColors();
		}
	}
}
