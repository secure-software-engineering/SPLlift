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
//@import soot.PatchingChain;
//@import soot.Unit;
//@import soot.Value;
//@import soot.jimple.AssignStmt;
//@
//@public class AssignmentsCounter extends BodyTransformer implements ICounter<Long>, IResettable {
//@
//@	private static final String PROPERTY_NAME = "assignments";
//@
//@	// Ignore assignments that have $tempN on the left hand side?
//@	private boolean ignoreTemp;
//@
//@	private long counter = 0;
//@
//@	private AbstractMetricsSink sink;
//@
//@	public AssignmentsCounter(AbstractMetricsSink sink, boolean ignoreTemp) {
//@		this.ignoreTemp = ignoreTemp;
//@		this.sink = sink;
//@	}
//@
//@	public AssignmentsCounter(boolean ignoreTemp) {
//@		this.ignoreTemp = ignoreTemp;
//@	}
//@
//@	@Override
//@	protected void internalTransform(Body body, String phase, Map opt) {
//@		PatchingChain<Unit> units = body.getUnits();
//@		int counterChunk = 0;
//@		for (Unit unit : units) {
//@			if (unit instanceof AssignStmt) {
//@				if (ignoreTemp) {
//@					AssignStmt assignment = (AssignStmt) unit;
//@					Value leftOp = assignment.getLeftOp();
//@					if (leftOp instanceof Local) {
//@						Local assignee = (Local) leftOp;
//@						if (!assignee.getName().contains("$")) {
//@							counterChunk++;
//@						}
//@					}
//@				} else {
//@					counterChunk++;
//@				}
//@			}
//@		}
//@		sink.flow(body, AssignmentsCounter.PROPERTY_NAME, counterChunk);
//@		counter += counterChunk;
//@	}
//@
//@	public Long getCount() {
//@		return counter;
//@	}
//@
//@	public void reset() {
//@		counter = 0;
//@	}
//@
//@}
// #endif
