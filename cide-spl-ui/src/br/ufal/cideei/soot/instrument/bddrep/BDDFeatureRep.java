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

import java.util.Set;

import net.sf.javabdd.BDD;

import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.IFeatureRep;

public class BDDFeatureRep implements IFeatureRep {
	
	BDD configs;
	
	public BDDFeatureRep clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IFeatureRep addAll(IFeatureRep rep) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean belongsToConfiguration(IConfigRep config) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public BDD getBDD() {
		return null;
	}
}
