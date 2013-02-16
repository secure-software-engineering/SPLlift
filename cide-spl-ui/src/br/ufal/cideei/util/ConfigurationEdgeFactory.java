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

package br.ufal.cideei.util;

import java.util.Set;

import org.jgrapht.EdgeFactory;

import soot.Unit;
import br.ufal.cideei.soot.instrument.IConfigRep;

public class ConfigurationEdgeFactory implements EdgeFactory<Unit, ValueContainerEdge<IConfigRep>> {

	static ConfigurationEdgeFactory instance = null;

	private ConfigurationEdgeFactory() {
	}

	public static ConfigurationEdgeFactory getInstance() {
		if (ConfigurationEdgeFactory.instance == null) {
			instance = new ConfigurationEdgeFactory();
		}
		return ConfigurationEdgeFactory.instance;
	}

	@Override
	public ValueContainerEdge<IConfigRep> createEdge(Unit source, Unit target) {
		return new ValueContainerEdge<IConfigRep>();
	}
}
