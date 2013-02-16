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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufal.cideei.features.IFeatureExtracter;

// TODO: Auto-generated Javadoc
/**
 * The Class LineNumberColorMapper.
 */
public class LineNumberColorMapper extends ASTVisitor {
	
	protected static long extractTime = 0;
	
	public static void reset() {
		extractTime = 0;
	}

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/** The file. */
	private IFile file;

	/** The extracter. */
	private IFeatureExtracter extracter;
	
	protected Map<Integer, Set<String>> lineToColors = new HashMap<Integer, Set<String>>();

	/**
	 * Gets the line to colors.
	 * 
	 * @return the line to colors
	 */
	public Map<Integer, Set<String>> getLineToColors() {
		return lineToColors;
	}

	/**
	 * Instantiates a new line number color mapper.
	 * 
	 * @param compilationUnit
	 *            the compilation unit
	 * @param file
	 *            the file
	 * @param extracter
	 *            the extracter
	 */
	public LineNumberColorMapper(CompilationUnit compilationUnit, IFile file, IFeatureExtracter extracter) {
		// dependencies
		this.compilationUnit = compilationUnit;
		this.file = file;
		this.extracter = extracter;
	}

	/**
	 * Defeats instantiation.
	 */
	private LineNumberColorMapper() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom
	 * .ASTNode)
	 */
	public boolean visit(MethodDeclaration methodDeclaration) {
		methodDeclaration.accept(new MethodBodyColorMapper(compilationUnit, file, extracter, lineToColors));
		return false;
	}

	public static long getExtractTime() {
		return extractTime;		
	}
}

class MethodBodyColorMapper extends ASTVisitor {
	
	private CompilationUnit compilationUnit;
	private IFile file;
	private IFeatureExtracter extracter;
	private Map<Integer, Set<String>> lineToColors;

	MethodBodyColorMapper(CompilationUnit compilationUnit, IFile file, IFeatureExtracter extracter, Map<Integer, Set<String>> lineToColors) {
		// dependencies
		this.compilationUnit = compilationUnit;
		this.file = file;
		this.extracter = extracter;
		this.lineToColors = lineToColors;
	}
	
	@Override
	public void preVisit(ASTNode node) {
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		Set<String> mappedFeatureSet = lineToColors.get(lineNumber);
		if (mappedFeatureSet == null) {
			
			long startExtract = System.nanoTime();
			Set<String> extractedFeatures = extracter.getFeaturesNames(node, file);
			long endExtract = System.nanoTime();
			long extractDelta = endExtract - startExtract;
			LineNumberColorMapper.extractTime += extractDelta;

			if (!extractedFeatures.isEmpty()) {
				lineToColors.put(lineNumber, extractedFeatures);
			}
		}
	}
	
}
