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
import java.util.List;
import java.util.Set;

import soot.toolkits.scalar.AbstractFlowSet;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import br.ufal.cideei.soot.instrument.IConfigRep;

/**
 * The Class LiftedFlowSet.
 * 
 * @param <T>
 *            the generic type
 */
//XXX refatorar!
public class LiftedFlowSet extends AbstractFlowSet {

	private int liftedFlowSetSize;

	private IConfigRep[] configurations;

	private FlowSet[] lattices;

	/**
	 * Instantiates a new LiftedFlowSet.
	 */
	public LiftedFlowSet(Collection<IConfigRep> configs) {
		this.liftedFlowSetSize = configs.size();

		// Both lists have the same size...
		// Configurations = [ {}, {A}, {B}, {A, B} ]
		// Lattices = [ l1, l2, l3, l4 ]

		this.configurations = new IConfigRep[liftedFlowSetSize];
		this.lattices = new FlowSet[liftedFlowSetSize];

		// Ugly... configs does not have a get method... :-(
		int i = 0;
		for (IConfigRep configuration : configs) {
			this.configurations[i] = configuration;
			this.lattices[i] = new ArraySparseSet();
			i++;
		}
	}

	/**
	 * Instantiates a new LiftedFlowSet, but copy the contents of the other into
	 * this. A pseudo copy-constructor.
	 * 
	 * @param other
	 *            the other
	 */
	public LiftedFlowSet(LiftedFlowSet other) {
		this.configurations = other.configurations.clone();
		this.lattices = other.lattices.clone();
		this.liftedFlowSetSize = other.liftedFlowSetSize;
	}

	private LiftedFlowSet() {
	}

	/**
	 * @return the configurations of this lifted lattice.
	 */
	public IConfigRep[] getConfigurations() {
		return configurations;
	}

	/**
	 * @return the normal lattices contained in this lifted lattice.
	 */
	public FlowSet[] getLattices() {
		return lattices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#clone()
	 */
	@Override
	public LiftedFlowSet clone() {
		LiftedFlowSet other = new LiftedFlowSet();
		other.configurations = this.configurations.clone();

		other.lattices = new FlowSet[this.lattices.length];
		for (int index = 0; index < lattices.length; index++) {
			other.lattices[index] = this.lattices[index].clone();
		}

		other.liftedFlowSetSize = this.liftedFlowSetSize;
		return other;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// A LiftedFlowSet can only be equal to another LiftedFlowSet
		if (!(obj instanceof LiftedFlowSet))
			return false;
		// Test for self-equality
		if (obj == this)
			return true;

		LiftedFlowSet other = (LiftedFlowSet) obj;

		// Deep-check equality
		boolean returnFlag = true;
		for (int i = 0; i < liftedFlowSetSize; i++) {
			if (!other.configurations[i].equals(this.configurations[i]) || !other.lattices[i].equals(this.lattices[i])) {
				returnFlag = false;
				break;
			}
		}

		return returnFlag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#clear()
	 */
	@Override
	public void clear() {
		this.configurations = null;
		this.lattices = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#add(java.lang.Object)
	 */
	@Override
	public void add(Object object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object object) {
		throw new UnsupportedOperationException("This method is not defined for a LiftedFlowSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return ((configurations.length == 0) && (lattices.length == 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#remove(java.lang.Object)
	 */
	@Override
	public void remove(Object object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#size()
	 */
	@Override
	public int size() {
		return this.liftedFlowSetSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#toList()
	 */
	@Override
	public List toList() {
		throw new UnsupportedOperationException("This method is not defined for a LiftedFlowSet");
	}

	/**
	 * Copies this Config-FlowSet mapping into dest.
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#copy(soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void copy(FlowSet dest) {
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		for (int i = 0; i < this.liftedFlowSetSize; i++) {
			FlowSet destNormal = (FlowSet) destLifted.lattices[i];
			FlowSet thisNormal = (FlowSet) lattices[i];

			thisNormal.copy(destNormal);
		}
	}

	/**
	 * The union between LiftedFlowSets is defined as the union between every
	 * FlowSets in @code{this} and the FlowSets in @code{other} with the same
	 * configuration.
	 * 
	 * The result is placed on @code{dest}. It`s keys are preserved, but its
	 * flowsets are cleared.
	 * 
	 * @see soot.toolkits.scalar.AbstractFlowSet#union(soot.toolkits.scalar.FlowSet,
	 *      soot.toolkits.scalar.FlowSet)
	 */
	@Override
	public void union(FlowSet other, FlowSet dest) {
		LiftedFlowSet otherLifted = (LiftedFlowSet) other;
		LiftedFlowSet destLifted = (LiftedFlowSet) dest;

		for (int i = 0; i < this.liftedFlowSetSize; i++) {
			FlowSet otherNormal = (FlowSet) otherLifted.lattices[i];
			FlowSet thisNormal = (FlowSet) lattices[i];

			FlowSet destNewFlowSet = new ArraySparseSet();
			destLifted.lattices[i] = destNewFlowSet;
			thisNormal.union(otherNormal, destNewFlowSet);
		}
	}

	/**
	 * Returns a String representation of this object.
	 * 
	 * @return String representation
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < this.liftedFlowSetSize; i++) {
			result.append(this.configurations[i].toString());
			result.append("=");
			result.append(this.lattices[i].toString());
			result.append("; ");
		}
		return result.toString();
	}

}