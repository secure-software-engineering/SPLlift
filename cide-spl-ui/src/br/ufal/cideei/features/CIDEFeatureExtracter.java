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

package br.ufal.cideei.features;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

import cide.gast.IASTNode;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.language.jdt.ASTBridge;

/**
 * A feature extracter implementation for CIDE.
 */
final class CIDEFeatureExtracter implements IFeatureExtracter {

	/**
	 * Instantiates a new CIDE feature extracter. Clients should use the
	 * {@link CIDEFeatureExtracterFactory#getExtracter()} method.
	 */
	CIDEFeatureExtracter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ufal.cideei.features.IFeatureExtracter#getFeaturesNames(org.eclipse
	 * .jdt.core.dom.ASTNode, org.eclipse.core.resources.IFile)
	 */
	@Override
	public Set<String> getFeaturesNames(ASTNode node, IFile file) {
		Set<IFeature> cideFeatureSet = this.getFeatures(node, file);
		Set<String> stringFeatureSet = new HashSet<String>(cideFeatureSet.size());
		for (IFeature feature : cideFeatureSet) {
			String featureName = feature.getName();
			if (!stringFeatureSet.contains(featureName)) {
				stringFeatureSet.add(featureName);
			}
		}
		return stringFeatureSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.ufal.cideei.features.IFeatureExtracter#getFeatures(org.eclipse.jdt
	 * .core.dom.ASTNode, org.eclipse.core.resources.IFile)
	 */
	@Override
	public Set<IFeature> getFeatures(ASTNode node, IFile file) {
		ColoredSourceFile coloredFile;
		try {
			coloredFile = ColoredSourceFile.getColoredSourceFile(file);
		} catch (FeatureModelNotFoundException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		IASTNode iASTNode = ASTBridge.bridge(node);
		Set<IFeature> cideFeatureSet = coloredFile.getColorManager().getColors(iASTNode);
		return cideFeatureSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.ufal.cideei.features.IFeatureExtracter#isValid(java.util.Set)
	 */
	@Override
	public boolean isValid(Set<IFeature> config) throws FeatureModelNotFoundException {
		/*
		 * FIXME: simply not working. WHY?
		 */
		// IFeatureModel featureModel =
		// FeatureModelManager.getInstance().getActiveFeatureModelProvider().getFeatureModel(javaProject.getProject());
		// return
		// FeatureModelManager.getInstance().getActiveFeatureModelProvider().getFeatureModel(javaProject.getProject()).isValidSelection(featureModel.getVisibleFeatures());
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
