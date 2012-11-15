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

package br.ufal.cideei.util.count;

import java.io.IOException;

import soot.Body;

public class MetricsSink extends AbstractMetricsSink {

	private MetricsTable table;

	public MetricsSink(MetricsTable table) {
		this.table = table;
	}

	@Override
	protected void handle(Body body, String property, String value) {
		table.setProperty(body.getMethod().getSignature(), property, value);
	}
	
	protected void handle(Body body, String property, double value) {
		table.setProperty(body.getMethod().getSignature(), property, value);
	}
	
	public void terminate() {
		try {
			table.dumpEntriesAndClose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}