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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CachedICompilationUnitParser {

	// Caching key
	private IFile file = null;

	// Caching target
	private CompilationUnit cu = null;

	// Expensive to create, reuse it.
	private ASTParser parser = null;

	public CompilationUnit parse(IFile aFile) {
		// MISS
		if (!aFile.equals(this.file)) {
//			System.out.println("CachedICompilationUnitParser: miss");
			/*
			 * Lazy initializes the member ASTParser
			 */
			if (parser == null) {
				parser = ASTParser.newParser(AST.JLS3);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setResolveBindings(true);
			}

			/*
			 * Create a ASTNode (a CompilationUnit) by reusing the parser;
			 */
			ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(aFile);
			parser.setSource(compilationUnit);
			CompilationUnit jdtCompilationUnit = (CompilationUnit) parser.createAST(null);

			/*
			 * Caches the result
			 */
			this.file = aFile;
			return this.cu = jdtCompilationUnit;
		} // HIT
		else {
			return this.cu;
		}
	}

}
