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

import java.util.Set;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class ConfigTag implements Tag {

	/** The Constant CONFIG_TAG_NAME. */
	public static final String CONFIG_TAG_NAME = "ConfigTag";
	private Set<IConfigRep> reps;

	public ConfigTag(Set<IConfigRep> localConfigs) {
		this.reps = localConfigs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.tagkit.Tag#getName()
	 */
	@Override
	public String getName() {
		return ConfigTag.CONFIG_TAG_NAME;
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

	public Set<IConfigRep> getConfigReps() {
		return this.reps;
	}
	
	public int size(){
		return reps.size();
	}

}
