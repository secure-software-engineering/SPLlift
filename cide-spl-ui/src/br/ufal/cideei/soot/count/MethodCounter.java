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
//@import java.util.Collection;
//@import java.util.Collections;
//@import java.util.HashSet;
//@import java.util.Map;
//@import java.util.Set;
//@
//@import soot.Body;
//@import soot.BodyTransformer;
//@import soot.SootMethod;
//@
//@public class MethodCounter extends BodyTransformer implements ICounter<Integer>, IResettable {
//@
//@	private static MethodCounter instance = null;
//@	
//@	private Set<SootMethod> methodContainer = new HashSet<SootMethod>();
//@
//@	private MethodCounter() {
//@	}
//@
//@	public static MethodCounter v() {
//@		if (instance == null)
//@			instance = new MethodCounter();
//@		return instance;
//@	}
//@	
//@	public Collection<SootMethod> getMethods(){
//@		return Collections.unmodifiableCollection(methodContainer);
//@	}
//@
//@	@Override
//@	protected void internalTransform(Body body, String phase, Map opt) {
//@		SootMethod method = body.getMethod();
//@		if (!methodContainer.contains(method)) {
//@			methodContainer.add(method);
//@		}
//@	}
//@
//@	@Override
//@	public Integer getCount() {
//@		return methodContainer.size();
//@	}
//@
//@	@Override
//@	public void reset() {
//@		methodContainer.clear();
//@	}
//@}
//#endif
