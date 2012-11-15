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

package br.ufal.cideei.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.text.ITextSelection;

import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;

public class GetFeatureVisitor extends ASTVisitor{
	/** The text selection. */
	private ITextSelection textSelection;
	private IFile file;
	
	/** The nodes will be added to this Set as the visitor visits the nodes*/
	private Set<ASTNode> nodes = new HashSet<ASTNode>();
	private Set<String> features;

	/**
	 * Instantiates a new selection nodes visitor.
	 */
	@SuppressWarnings("unused")
	private GetFeatureVisitor() {
	}

	/**
	 * Instantiates a new selection nodes visitor.
	 *
	 * @param textSelection the text selection
	 */
	public GetFeatureVisitor(ITextSelection textSelection, IFile file) {
		this.textSelection = textSelection;
		this.file = file;
	}
	
	/**
	 * Gets the nodes. 
	 *
	 * @return the nodes
	 */
	public Set<ASTNode> getNodes(){
		return nodes;
	}
	
	public Set<String> getFeatures(){
		return features;
	}

	/**
	 * Populates the {@link #nodes} Set with the ASTNodes. 
	 * Use {@link #getNodes()} to retrive the nodes after accepting this visitor to an ASTNode
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		if (node.getStartPosition() >= textSelection.getOffset() && 
				(node.getStartPosition() + node.getLength()) <= textSelection.getOffset() + textSelection.getLength()) {
			IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().getExtracter();
			features = extracter.getFeaturesNames(node, file);
			return;
		}
	}
	
	
	
}
