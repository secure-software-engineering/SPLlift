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

package br.ufal.cideei.soot.instrument.bitrep;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;
import org.apache.commons.lang.builder.HashCodeBuilder;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

public final class BitConfigRep implements IConfigRep {

	private final int id;
	// String->int
	private UnmodifiableBidiMap atoms;
	private int hashCode;

	public BitConfigRep(int index, UnmodifiableBidiMap atoms) {
		this.atoms = atoms;
		this.id = index;
		this.hashCode = new HashCodeBuilder(17, 31).append(id).toHashCode();
	}

	public BitConfigRep(int index, BidiMap atoms) {
		this.atoms = (UnmodifiableBidiMap) UnmodifiableBidiMap.decorate(atoms);
		id = index;
		this.hashCode = new HashCodeBuilder(17, 31).append(id).toHashCode();
	}

	public static SetBitConfigRep localConfigurations(int highestId, UnmodifiableBidiMap atoms) {
		Set<IConfigRep> localConfigs = new HashSet<IConfigRep>();
		for (int index = 0; index < highestId; index++) {
			localConfigs.add(new BitConfigRep(index, atoms));
		}
		return new SetBitConfigRep(localConfigs, atoms, highestId);
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof BitConfigRep))
			return false;
		BitConfigRep that = (BitConfigRep) o;
		return this.id == that.id;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean belongsToConfiguration(IFeatureRep rep) {
		if (rep instanceof BitFeatureRep) {
			BitFeatureRep bitRep = (BitFeatureRep) rep;
			int repId = bitRep.getId();
			return ((repId & this.id) == repId);
		} else
			throw new IllegalArgumentException();
	}

	@Override
	public int size() {
		return Integer.bitCount(id);
	}

	@Override
	public String toString() {
		return "" + id;
	}
}
