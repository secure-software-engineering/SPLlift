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

import java.util.BitSet;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * The Class FeatureTag is used to store feature-sensitive metadata. The metadata is simply stored in a Set collection.
 * 
 * @param <IFeatureRep>
 *            the element type
 */
public class FeatureTag implements Tag {

	/** The Constant FEAT_TAG_NAME. */
	public static final String FEAT_TAG_NAME = "FeatureTag";

	BitSet rep;

	public FeatureTag(BitSet bs) {
		this.rep = bs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.tagkit.Tag#getName()
	 */
	@Override
	public String getName() {
		return FeatureTag.FEAT_TAG_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.tagkit.Tag#getValue()
	 */
	@Override
	public byte[] getValue() throws AttributeValueException {
		return null;
	}

	public BitSet getFeatureRep() {
		return this.rep;
	}

	@Override
	public String toString() {
		return this.rep.toString();
	}
}