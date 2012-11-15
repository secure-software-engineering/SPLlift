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

package br.ufal.cideei.soot.analyses;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.soot.instrument.bitrep.BitVectorConfigRep;

public class MapLiftedFlowSet extends AbstractFlowSet {
	protected HashMap<IConfigRep, FlowSet> map;

	public HashMap<IConfigRep, FlowSet> getMapping() {
		return map;
	}

	protected MapLiftedFlowSet(Map<IConfigRep, FlowSet> map) {
		this.map = new HashMap<IConfigRep, FlowSet>(map);
	}

	public MapLiftedFlowSet(Collection<IConfigRep> configs) {
		map = new HashMap<IConfigRep, FlowSet>();
		for (IConfigRep config : configs) {
			map.put(config, new ArraySparseSet());
		}
	}

	public MapLiftedFlowSet(IConfigRep seed) {
		map = new HashMap<IConfigRep, FlowSet>();
		map.put(seed, new ArraySparseSet());
	}

	public FlowSet getLattice(IConfigRep config) {
		return this.map.get(config);
	}

	@Override
	public MapLiftedFlowSet clone() {
		Set<Entry<IConfigRep, FlowSet>> entrySet = map.entrySet();
		Map<IConfigRep, FlowSet> newMap = new HashMap<IConfigRep, FlowSet>();
		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
			newMap.put(entry.getKey(), entry.getValue().clone());
		}
		return new MapLiftedFlowSet(newMap);
	}

	@Override
	public void copy(FlowSet dest) {
		MapLiftedFlowSet destLifted = (MapLiftedFlowSet) dest;
		dest.clear();
		Set<Entry<IConfigRep, FlowSet>> entrySet = map.entrySet();
		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
			IConfigRep key = entry.getKey();
			FlowSet value = entry.getValue();
			destLifted.map.put(key, value.clone());
		}
	}

	public Set<IConfigRep> getConfigurations() {
		return map.keySet();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;
		if (!(o instanceof MapLiftedFlowSet))
			return false;
		MapLiftedFlowSet that = (MapLiftedFlowSet) o;
		return new EqualsBuilder().append(this.map, that.map).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(this.map).toHashCode();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public void union(FlowSet aOther, FlowSet aDest) {
		// #ifdef LAZY
//@		MapLiftedFlowSet other = (MapLiftedFlowSet) aOther;
//@		MapLiftedFlowSet dest = (MapLiftedFlowSet) aDest;
//@
//@		Set<Entry<IConfigRep, FlowSet>> entrySet = this.map.entrySet();
//@		Set<Entry<IConfigRep, FlowSet>> otherEntrySet = other.map.entrySet();
//@
//@		HashMap<IConfigRep, FlowSet> destMap = new HashMap<IConfigRep, FlowSet>();
//@
//@		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
//@			for (Entry<IConfigRep, FlowSet> otherEntry : otherEntrySet) {
//@				ILazyConfigRep key = (ILazyConfigRep) entry.getKey();
//@				ILazyConfigRep otherKey = (ILazyConfigRep) otherEntry.getKey();
//@
//@				ILazyConfigRep intersection = key.intersection(otherKey);
//@				if (intersection.size() != 0) {
//@					FlowSet otherFlowSet = otherEntry.getValue();
//@					ArraySparseSet destFlowSet = new ArraySparseSet();
//@					entry.getValue().union(otherFlowSet, destFlowSet);
//@					destMap.put(intersection, destFlowSet);
//@				}
//@			}
//@		}
//@
//@		dest.map = destMap;
//@
		// #else
		 MapLiftedFlowSet otherLifted = (MapLiftedFlowSet) aOther;
		 MapLiftedFlowSet destLifted = (MapLiftedFlowSet) aDest;
		
		 Set<Entry<IConfigRep, FlowSet>> entrySet = map.entrySet();
		 for (Entry<IConfigRep, FlowSet> entry : entrySet) {
		 // key
		 IConfigRep config = entry.getKey();
		 // val
		 FlowSet thisNormal = entry.getValue();
		
		 FlowSet otherNormal = otherLifted.map.get(config);
		 if (otherNormal == null) {
		 otherNormal = new ArraySparseSet();
		 }
		
		 ArraySparseSet destNewFlowSet = new ArraySparseSet();
		 destLifted.map.put(config, destNewFlowSet);
		 thisNormal.union(otherNormal, destNewFlowSet);
		 }
		// #endif
	}

	@Override
	public void intersection(FlowSet aOther, FlowSet aDest) {
		// #ifdef LAZY
//@		MapLiftedFlowSet other = (MapLiftedFlowSet) aOther;
//@		MapLiftedFlowSet dest = (MapLiftedFlowSet) aDest;
//@
//@		Set<Entry<IConfigRep, FlowSet>> entrySet = this.map.entrySet();
//@		Set<Entry<IConfigRep, FlowSet>> otherEntrySet = other.map.entrySet();
//@
//@		HashMap<IConfigRep, FlowSet> destMap = new HashMap<IConfigRep, FlowSet>();
//@
//@		for (Entry<IConfigRep, FlowSet> entry : entrySet) {
//@			for (Entry<IConfigRep, FlowSet> otherEntry : otherEntrySet) {
//@				ILazyConfigRep key = (ILazyConfigRep) entry.getKey();
//@				ILazyConfigRep otherKey = (ILazyConfigRep) otherEntry.getKey();
//@
//@				ILazyConfigRep intersection = key.intersection(otherKey);
//@				if (intersection.size() != 0) {
//@					FlowSet otherFlowSet = otherEntry.getValue();
//@					ArraySparseSet destFlowSet = new ArraySparseSet();
//@					entry.getValue().intersection(otherFlowSet, destFlowSet);
//@					destMap.put(intersection, destFlowSet);
//@				}
//@			}
//@		}
//@
//@		dest.map = destMap;
//@
		// #else
		 MapLiftedFlowSet otherLifted = (MapLiftedFlowSet) aOther;
		 MapLiftedFlowSet destLifted = (MapLiftedFlowSet) aDest;
		
		 Set<Entry<IConfigRep, FlowSet>> entrySet = map.entrySet();
		 for (Entry<IConfigRep, FlowSet> entry : entrySet) {
		 // key
		 IConfigRep config = entry.getKey();
		 // val
		 FlowSet thisNormal = entry.getValue();
		
		 FlowSet otherNormal = otherLifted.map.get(config);
		 if (otherNormal == null) {
		 otherNormal = new ArraySparseSet();
		 }
		
		 ArraySparseSet destNewFlowSet = new ArraySparseSet();
		 destLifted.map.put(config, destNewFlowSet);
		 thisNormal.intersection(otherNormal, destNewFlowSet);
		 }
		// #endif
	}

	public FlowSet add(IConfigRep config, FlowSet flow) {
		return map.put(config, flow);
	}

	@Override
	public void add(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public List toList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
