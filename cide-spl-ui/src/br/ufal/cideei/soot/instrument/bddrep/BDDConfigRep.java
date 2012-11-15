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

package br.ufal.cideei.soot.instrument.bddrep;

import java.util.Collection;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;
import br.ufal.cideei.util.Pair;

public class BDDConfigRep implements ILazyConfigRep {

	private UnmodifiableBidiMap atoms;
	private BDD configs;

	private BDDConfigRep(BDD configsAsBDD, UnmodifiableBidiMap atoms) {
		this.configs = configsAsBDD;
		this.atoms = atoms;
	}

	public static BDDConfigRep localConfigurations(UnmodifiableBidiMap atoms, BDDFactory factory) {
		return new BDDConfigRep(factory.one(), atoms);
	}

	@Override
	public ILazyConfigRep intersection(ILazyConfigRep aOther) {
		if (aOther instanceof BDDConfigRep) {
			BDDConfigRep other = (BDDConfigRep) aOther;
			return new BDDConfigRep(other.configs.and(this.configs), atoms);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(IFeatureRep aRep) {
		if (aRep instanceof BDDFeatureRep) {
			BDDFeatureRep rep = (BDDFeatureRep) aRep;
			BDD repBDD = rep.getBDD();
			return new Pair<ILazyConfigRep, ILazyConfigRep>(new BDDConfigRep(this.configs.and(repBDD), atoms), new BDDConfigRep(this.configs.and(repBDD.not()), atoms));
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Pair<ILazyConfigRep, ILazyConfigRep> split(Collection<IConfigRep> belongedConfigs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILazyConfigRep union(ILazyConfigRep aOther) {
		if (aOther instanceof BDDConfigRep) {
			BDDConfigRep other = (BDDConfigRep) aOther;
			return new BDDConfigRep(other.configs.or(this.configs), atoms);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public int size() {
		return configs.nodeCount();
	}

	@Override
	public boolean belongsToConfiguration(IFeatureRep rep) {
		throw new UnsupportedOperationException();
	}

}
