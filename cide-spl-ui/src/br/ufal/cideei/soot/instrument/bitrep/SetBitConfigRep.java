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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

public class SetBitConfigRep implements ILazyConfigRep {

	private UnmodifiableBidiMap atoms;
	private Set<IConfigRep> configs;
	private final int highestId;

	public SetBitConfigRep(Collection<IConfigRep> configs, BidiMap atoms, int highestId) {
		this.atoms = (UnmodifiableBidiMap) UnmodifiableBidiMap.decorate(atoms);
		this.configs = Collections.unmodifiableSet(new HashSet<IConfigRep>(configs));
		this.highestId = highestId;
	}

	@Override
	public boolean belongsToConfiguration(IFeatureRep rep) {
		for (IConfigRep config : configs) {
			if (config.belongsToConfiguration(rep)) {
				return true;
			}
		}
		return false;
	}
	
	public Set<IConfigRep> getConfigs() {
		return configs;
	}

	private Set<IConfigRep> belongsToConfigurations(IFeatureRep rep) {
		Set<IConfigRep> foundConfigs = new HashSet<IConfigRep>();
		for (IConfigRep config : configs) {
			if (config.belongsToConfiguration(rep)) {
				foundConfigs.add(config);
			}
		}
		return foundConfigs;
	}

	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep rep) {
		Set<IConfigRep> belongsToConfigurations = belongsToConfigurations(rep);

		Set<IConfigRep> leftSplit = new HashSet<IConfigRep>(configs);
		leftSplit.removeAll(belongsToConfigurations);

		return new Pair<ILazyConfigRep, ILazyConfigRep>(new SetBitConfigRep(leftSplit, atoms, highestId), new SetBitConfigRep(belongsToConfigurations, atoms,
				highestId));
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs) {
		Set<IConfigRep> leftSplit = new HashSet<IConfigRep>(configs);
		leftSplit.removeAll(belongedConfigs);

		return new Pair<ILazyConfigRep, ILazyConfigRep>(new SetBitConfigRep(leftSplit, atoms, highestId),
				new SetBitConfigRep(belongedConfigs, atoms, highestId));
	}

	@Override
	public int size() {
		return this.configs.size();
	}

	@Override
	public String toString() {
		return configs.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof SetBitConfigRep))
			return false;
		SetBitConfigRep that = (SetBitConfigRep) o;
		return new EqualsBuilder().append(this.configs, that.configs).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.configs).toHashCode();
	}

	@Override
	public SetBitConfigRep intersection(ILazyConfigRep aOther) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SetBitConfigRep union(ILazyConfigRep otherKey) {
		throw new UnsupportedOperationException();
	}
}
