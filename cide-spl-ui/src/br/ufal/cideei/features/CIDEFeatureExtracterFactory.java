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

/*
 * 
 */
package br.ufal.cideei.features;

/**
 * A factory for creating CIDEFeatureExtracter objects. But as CIDEFeatureExtracter objects are immutable classes and
 * contains no state, this class simply provides a way to retrieve the single CIDEFeatureExtracter instance needed.
 */
public class CIDEFeatureExtracterFactory {

	/** The instance. */
	static CIDEFeatureExtracterFactory instance = new CIDEFeatureExtracterFactory();

	static CIDEFeatureExtracter extracterInstance = new CIDEFeatureExtracter();

	/**
	 * Defeats instantiation.
	 */
	private CIDEFeatureExtracterFactory() {
	}

	/**
	 * Gets the single instance of CIDEFeatureExtracterFactory.
	 * 
	 * @return single instance of CIDEFeatureExtracterFactory
	 */
	public static CIDEFeatureExtracterFactory getInstance() {
		return instance;
	}

	/**
	 * Returns an object that implements the IFeatureExtracter interface.
	 * 
	 * @return the feature extracter
	 */
	public IFeatureExtracter getExtracter() {
		return extracterInstance;
	}
}
