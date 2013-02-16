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

import soot.Body;
import soot.Unit;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class DebugUtil {

	public void f(Body body, ForwardFlowAnalysis<Unit, FlowSet> analysis) {
		if (body.getMethod().getSignature().contains("simple3")) {
			System.out.println(body.getTag(ConfigTag.CONFIG_TAG_NAME));
			for (Unit unit : body.getUnits()) {
				System.out.println(unit + " [[" + unit.getTag(FeatureTag.FEAT_TAG_NAME) + "]]");
				System.out.println(analysis.getFlowAfter(unit));
			}
		}
	}

//	public void f2(Body body, LiftedReachingDefinitions analysis) {
//		DOTExporter<Unit, ValueContainerEdge<IConfigRep>> exporter = new DOTExporter<Unit, ValueContainerEdge<IConfigRep>>(new VertexLineNameProvider<Unit>(null), null, new
//
//		ConfigurationEdgeNameProvider<ValueContainerEdge<IConfigRep>>());
//		try {
//			exporter.export(new FileWriter(System.getProperty("user.home") + File.separator + "REACHES DATA" + ".dot"), createProvidesGraph(body.getUnits(), analysis, body));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

//	private DirectedMultigraph<Unit, ValueContainerEdge<IConfigRep>> createProvidesGraph(Collection<Unit> unitsInSelection, LiftedReachingDefinitions reachingDefinitions, Body body) {
//		ConfigTag configTag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
//
//		DirectedMultigraph<Unit, ValueContainerEdge<IConfigRep>> reachesData = new DirectedMultigraph<Unit, ValueContainerEdge<IConfigRep>>(ConfigurationEdgeFactory.getInstance());
//
//		// for every unit in the selection...
//		for (Unit unitFromSelection : unitsInSelection) {
//			if (unitFromSelection instanceof DefinitionStmt) {
//				/*
//				 * exclude definitions when it's $temp on the leftOp.
//				 */
//				DefinitionStmt definition = (DefinitionStmt) unitFromSelection;
//
//				Value leftOp = (Value) definition.getLeftOp();
//				if (leftOp instanceof Local) {
//					if (((Local) leftOp).getName().contains("$")) {
//						continue;
//					}
//				}
//
//				IFeatureRep featuresThatUseDefinition = null;
//
//				// for every unit in the body...
//				Iterator<Unit> iterator = body.getUnits().snapshotIterator();
//				while (iterator.hasNext()) {
//					Unit nextUnit = iterator.next();
//					FeatureTag nextUnitTag = (FeatureTag) nextUnit.getTag("FeatureTag");
//
//					List useAndDefBoxes = nextUnit.getUseAndDefBoxes();
//					for (Object object : useAndDefBoxes) {
//						ValueBox vbox = (ValueBox) object;
//						if (vbox.getValue().equivTo(leftOp)) {
//							if (featuresThatUseDefinition == null) {
//								featuresThatUseDefinition = nextUnitTag.getFeatureRep().clone();
//							} else {
//								featuresThatUseDefinition.addAll(nextUnitTag.getFeatureRep());
//							}
//						}
//					}
//
//					MapLiftedFlowSet liftedFlowAfter = (MapLiftedFlowSet) reachingDefinitions.getFlowAfter(nextUnit);
//					Collection<IConfigRep> configurations = liftedFlowAfter.getConfigurations();
//					for (IConfigRep currConfiguration : configurations) {
//						FlowSet flowSet = liftedFlowAfter.getLattice(currConfiguration);
//
//						// if the unit belongs to the current configuration...
//						if (!nextUnitTag.getFeatureRep().belongsToConfiguration(currConfiguration)) {
//							continue;
//						}
//
//						// if the definition reaches this unit...
//						if (flowSet.contains(definition)) {
//							List<ValueBox> useBoxes = nextUnit.getUseBoxes();
//							for (ValueBox vbox : useBoxes) {
//								/*
//								 * and the definition is used, add to the map (graph)...
//								 */
//								if (vbox.getValue().equivTo(leftOp)) {
//									if (!reachesData.containsVertex(definition)) {
//										reachesData.addVertex(definition);
//									}
//									if (!reachesData.containsVertex(nextUnit)) {
//										reachesData.addVertex(nextUnit);
//									}
//
//									Set<ValueContainerEdge<IConfigRep>> allEdges = reachesData.getAllEdges(definition, nextUnit);
//									if (allEdges.size() >= 1) {
//										int diffCounter = 0;
//										Iterator<ValueContainerEdge<IConfigRep>> edgesIterator = allEdges.iterator();
//										Set<ValueContainerEdge<IConfigRep>> edgeRemovalSchedule = new HashSet<ValueContainerEdge<IConfigRep>>();
//										while (edgesIterator.hasNext()) {
//											ValueContainerEdge<IConfigRep> valueContainerEdge = (ValueContainerEdge<IConfigRep>) edgesIterator.next();
//											IConfigRep valueConfiguration = valueContainerEdge.getValue();
//											FlowSet flowSetFromOtherReached = liftedFlowAfter.getLattice(valueConfiguration);
//
//											if (flowSetFromOtherReached.equals(flowSet)) {
//												/*
//												 * Se a configura��o que estiver "querendo" entrar for menor,
//												 * ent�o ela expulsar� os maiores.
//												 */
//												if (valueConfiguration.size() > currConfiguration.size() && featuresThatUseDefinition.belongsToConfiguration(currConfiguration)) {
//													edgeRemovalSchedule.add(valueContainerEdge);
//													ValueContainerEdge<IConfigRep> addEdge = reachesData.addEdge(definition, nextUnit);
//													addEdge.setValue(currConfiguration);
//													continue;
//												}
//											} else {
//												diffCounter++;
//											}
//										}
//										if (diffCounter == allEdges.size() && featuresThatUseDefinition.belongsToConfiguration(currConfiguration)) {
//											ValueContainerEdge<IConfigRep> addEdge = reachesData.addEdge(definition, nextUnit);
//											addEdge.setValue(currConfiguration);
//										}
//										reachesData.removeAllEdges(edgeRemovalSchedule);
//									} else {
//										ValueContainerEdge<IConfigRep> addEdge = reachesData.addEdge(definition, nextUnit);
//										addEdge.setValue(currConfiguration);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		return reachesData;
//	}
}
