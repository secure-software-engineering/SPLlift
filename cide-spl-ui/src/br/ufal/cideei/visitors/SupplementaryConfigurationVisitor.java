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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;

public class SupplementaryConfigurationVisitor extends ASTVisitor {

	/** The text selection. */
	private HashSet<String> configuration;
	private HashMap<String,Set<ASTNode>> featureLines;
	private Set<String> featureNames;
	private IFile file;
	private IFeatureExtracter extracter;

	/**
	 * Instantiates a new selection nodes visitor.
	 */
	@SuppressWarnings("unused")
	private SupplementaryConfigurationVisitor() {
	}

	/**
	 * Instantiates a new selection nodes visitor.
	 *
	 * @param textSelection the text selection
	 */
	public SupplementaryConfigurationVisitor(HashSet<String> configuration, IFile file) {
		this.configuration = configuration;
		this.featureLines = new HashMap<String,Set<ASTNode>>();
		this.featureNames = new HashSet<String>();
		this.extracter = CIDEFeatureExtracterFactory.getInstance().getExtracter();
		this.file = file;
	}
	
	/**
	 * Gets the nodes. 
	 *
	 * @return the nodes
	 */
	public HashMap<String,Set<ASTNode>> getFeatureLines(){
		System.out.println(featureLines);
		return featureLines;
	}
	
	public Set<String> getFeatureNames(){
		System.out.println(featureNames);
		return featureNames;
	}

	/**
	 * Populates the {@link #nodes} Set with the ASTNodes. 
	 * Use {@link #getNodes()} to retrive the nodes after accepting this visitor to an ASTNode
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		Set<String> nodeFeatures = this.extracter.getFeaturesNames(node, this.file);
		if (!nodeFeatures.isEmpty()){
			if(!this.configuration.containsAll(nodeFeatures)) {
				Iterator<String> features = nodeFeatures.iterator();
				String f = null;
				if(nodeFeatures.size() > 1){
					while(features.hasNext()){
						f = features.next();
						this.addNode(node, f);
					}
				}else{
					f = features.next();
					this.addNode(node, f);
				}
			}
		}
	}
	
	private void addNode(ASTNode node, String feature){
		Set<ASTNode> nodes = featureLines.get(feature);
		if(nodes != null){
			 nodes.add(node);
		}else{
			nodes = new HashSet<ASTNode>();
			nodes.add(node);
		}
		featureNames.add(feature);
		featureLines.put(feature, nodes);
	}

}

