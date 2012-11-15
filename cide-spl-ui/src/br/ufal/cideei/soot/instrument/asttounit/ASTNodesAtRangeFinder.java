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

package br.ufal.cideei.soot.instrument.asttounit;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Unit;
import soot.tagkit.SourceLnPosTag;

/**
 * The Class ASTNodesAtRangeFinder maps ASTNodes to Units using their positions
 * (line and column) in the source code as a parameter.
 */
public class ASTNodesAtRangeFinder extends ASTVisitor {

	/** The compilation unit. */
	private CompilationUnit compilationUnit;

	/** The found nodes. */
	private Collection<ASTNode> foundNodes = new HashSet<ASTNode>();

	/** The starting line. */
	private int startLine;

	/** The starting position. */
	private int startPos;

	/** The ending line. */
	private int endLine;

	/** The ending position. */
	private int endPos;

	/**
	 * Instantiates a new ASTNodes at range finder.
	 * 
	 * @param startLine
	 *            the starting line
	 * @param startPos
	 *            the starting posision
	 * @param endLine
	 *            the ending line
	 * @param endPos
	 *            the ending position
	 * @param compilationUnit
	 *            the compilation unit in which the nodes will be visited
	 */
	public ASTNodesAtRangeFinder(int startLine, int startPos, int endLine, int endPos, CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.startLine = startLine;
		this.startPos = startPos;
		this.endLine = endLine;
		this.endPos = endPos;
	}

	/**
	 * Instantiates a new ASTNodes at range finder. The Unit MUST have the
	 * SourceLnPosTag tag attached to it, or an {@link IllegalArgumentException}
	 * will be thrown.
	 * 
	 * @param unit
	 *            the unit
	 * @param compilationUnit
	 *            the compilation unit
	 */
	public ASTNodesAtRangeFinder(Unit unit, CompilationUnit compilationUnit) {
		if (unit.hasTag("SourceLnPosTag")) {
			SourceLnPosTag lineTag = (SourceLnPosTag) unit.getTag("SourceLnPosTag");
			this.startLine = lineTag.startLn();
			this.startPos = lineTag.startPos();
			this.endLine = lineTag.endLn();
			this.endPos = lineTag.endPos();
			this.compilationUnit = compilationUnit;
		} else {
			throw new IllegalArgumentException("No SourceLnPosTag found in this unit.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom
	 * .ASTNode)
	 */
	@Override
	public void preVisit(ASTNode node) {
		int nodeStartPosition = node.getStartPosition();
		int nodeEndPosition = nodeStartPosition + node.getLength();

		if (compilationUnit.getLineNumber(nodeStartPosition) == startLine && compilationUnit.getColumnNumber(nodeStartPosition) + 1 >= startPos
				&& compilationUnit.getLineNumber(nodeEndPosition) == endLine && compilationUnit.getColumnNumber(nodeEndPosition) - 1 <= endPos) {

			this.foundNodes.add(node);
		}
	}

	/**
	 * Gets the nodes found. It will be empty before visiting.
	 * 
	 * @return the nodes
	 */
	public Collection<ASTNode> getNodes() {
		return this.foundNodes;
	}
}
