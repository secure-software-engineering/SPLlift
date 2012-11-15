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

//#ifdef METRICS
//@package br.ufal.cideei.soot.count;
//@
//@import java.util.Map;
//@
//@import br.ufal.cideei.util.count.AbstractMetricsSink;
//@
//@import soot.Body;
//@import soot.BodyTransformer;
//@import soot.Local;
//@import soot.util.Chain;
//@
//@public class LocalCounter extends BodyTransformer implements ICounter<Long>, IResettable {
//@
//@	private static final String LOCALS = "locals";
//@	private long counter = 0;
//@	private boolean excludeTemp = true;
//@	private AbstractMetricsSink sink;
//@
//@	public LocalCounter(AbstractMetricsSink sink, boolean excludeTemp) {
//@		this.excludeTemp = excludeTemp;
//@		this.sink = sink;
//@	}
//@
//@	public Long getCount() {
//@		return counter;
//@	}
//@
//@	@Override
//@	protected void internalTransform(Body body, String phase, Map opt) {
//@		int counterChunk = 0;
//@		if (excludeTemp) {
//@			Chain<Local> locals = body.getLocals();
//@			for (Local local : locals) {
//@				String name = local.getName();
//@				if (name != "this" && name.indexOf("$") == -1) {
//@					counterChunk++;
//@				}
//@			}
//@		} else {
//@			counterChunk = body.getLocalCount();
//@		}
//@		if (sink != null) {
//@			sink.flow(body, LOCALS, counterChunk);
//@		}
//@		counter += counterChunk;
//@	}
//@
//@	@Override
//@	public void reset() {
//@		counter = 0;
//@	}
//@
//@}
// #endif
