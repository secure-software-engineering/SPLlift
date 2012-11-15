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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

public class BitFeatureRep implements IFeatureRep, Cloneable {

	private final int id;

	private Set<String> features;

	private int hashCode;

	private final BidiMap originalFeatureIds;

	public int getId() {
		return id;
	}

	public BitFeatureRep(Set<String> features, BidiMap originalFeatureIds) {
		this.features = features;
		this.originalFeatureIds = originalFeatureIds;
		int accumulator = 0;
		for (String element : this.features) {
			Integer featId = (Integer) originalFeatureIds.get(element);
			if (featId != null) {
				accumulator += featId;
			}
		}
		this.id = accumulator;
		this.hashCode = new HashCodeBuilder(17, 31).append(id).toHashCode();
	}

	private BitFeatureRep(Set<String> features, int id, BidiMap originalFeatureIds) {
		this.features = features;
		this.id = id;
		this.originalFeatureIds = originalFeatureIds;
		this.hashCode = new HashCodeBuilder(17, 31).append(id).toHashCode();
	}
	
	@Override
	public int size() {
		return features.size();
	}

	@Override
	public Set<String> getFeatures() {
		return features;
	}

	@Override
	public boolean belongsToConfiguration(IConfigRep config) {
		return config.belongsToConfiguration(this);
	}

	@Override
	public BitFeatureRep addAll(IFeatureRep rep) {
		if (rep instanceof BitFeatureRep) {
			BitFeatureRep bitRep = (BitFeatureRep) rep;
			return new BitFeatureRep(rep.getFeatures(), this.id & bitRep.getId(), originalFeatureIds);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public IFeatureRep clone() {
		try {
			return (IFeatureRep) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof BitFeatureRep))
			return false;
		BitFeatureRep that = (BitFeatureRep) o;
		return new EqualsBuilder().append(this.id, that.id).isEquals();
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toString() {
		return this.id + ": " + this.features;
	}

	public BitFeatureRep intersectWith(BitFeatureRep other) {
		Set<String> intersection = new HashSet<String>(features);
		features.retainAll(other.features);
		return new BitFeatureRep(intersection, originalFeatureIds);
	}

	//
	// public void setFeatureIdMap(BidiMap atoms) {
	// this.atoms = atoms;
	// }
	//
	// /**
	// * Para um dado ID, retorna o conjunto de Features.
	// *
	// * @param id
	// * @return
	// */
	// public Set<IRep> getConfigurationForId(Integer id) {
	// Set<IRep> configuration = new HashSet<IRep>();
	// int highestOneBit = Integer.highestOneBit(id);
	// configuration.add((IRep) atoms.getKey(highestOneBit));
	// int tmp = id - highestOneBit;
	// while (tmp >= 1) {
	// highestOneBit = Integer.highestOneBit(tmp);
	// tmp -= highestOneBit;
	// configuration.add((IRep) atoms.getKey(highestOneBit));
	// }
	// return configuration;
	// }
	//
	// public Integer getIdForConfiguration(Set<IRep> configuration) {
	// Iterator<IRep> iterator = configuration.iterator();
	// int accumulator = 0;
	// while (iterator.hasNext()) {
	// IRep e = (IRep) iterator.next();
	// accumulator += (Integer) atoms.get(e);
	// }
	// return accumulator;
	// }

}
