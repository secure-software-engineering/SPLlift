package soot.spl.ifds;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.interproc.ifds.EdgeFunction;
import soot.jimple.interproc.ifds.EdgeFunctions;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.IFDSTabulationProblem;
import soot.jimple.interproc.ifds.JoinLattice;
import soot.jimple.interproc.ifds.ZeroedFlowFunctions;
import soot.jimple.interproc.ifds.edgefunc.EdgeIdentity;
import soot.jimple.interproc.ifds.solver.IDESolver;
import soot.jimple.interproc.ifds.template.DefaultIDETabulationProblem;
import soot.tagkit.Host;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class SPLIFDSSolver<D> extends IDESolver<Unit,D,SootMethod,Constraint<String>,ExtendedInterproceduralCFG> {
	
	protected final Constraint<String> FULL_FM_CONSTRAINT;
	
	/**
	 * Creates a solver for the given problem. The solver must then be started by calling
	 * {@link #solve()}.
	 * @param fullFmConstraint 
	 * @param alloyFilePath 
	 * @param numFeaturesPresent 
	 */
	public SPLIFDSSolver(final IFDSTabulationProblem<Unit,D,SootMethod,ExtendedInterproceduralCFG> ifdsProblem, final FeatureModelContext fmContext, final boolean useFMInEdgeComputations) {
		super(new DefaultIDETabulationProblem<D,Constraint<String>,ExtendedInterproceduralCFG>(new ExtendedInterproceduralCFG(ifdsProblem.interproceduralCFG())) {

			public FlowFunctions<Unit,D,SootMethod> createFlowFunctionsFactory() {
				return new FlowFunctions<Unit,D,SootMethod>() {

					@Override
					public FlowFunction<D> getNormalFlowFunction(Unit curr, Unit succ) {
						FlowFunction<D> original = ifdsProblem.flowFunctions().getNormalFlowFunction(curr, succ);
						if(hasFeatureAnnotation(curr) && interproceduralCFG().isFallThroughSuccessor(curr, succ)) {
							return new WrappedFlowFunction<D>(original);
						} else {
							return original;
						}
					}

					@Override
					public FlowFunction<D> getCallFlowFunction(Unit callStmt, SootMethod destinationMethod) {
						return ifdsProblem.flowFunctions().getCallFlowFunction(callStmt, destinationMethod);
					}

					@Override
					public FlowFunction<D> getReturnFlowFunction(Unit callSite, SootMethod calleeMethod, Unit exitStmt, Unit returnSite) {
						return ifdsProblem.flowFunctions().getReturnFlowFunction(callSite, calleeMethod, exitStmt, returnSite);
					}

					@Override
					public FlowFunction<D> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
						FlowFunction<D> original = ifdsProblem.flowFunctions().getCallToReturnFlowFunction(callSite, returnSite);
						if(hasFeatureAnnotation(callSite)) {
							return new WrappedFlowFunction<D>(original);
						} else {
							return original;
						}
					}
				};
			}
			
			public Set<Unit> initialSeeds() {
				return ifdsProblem.initialSeeds();
			}

			public D createZeroValue() {
				return ifdsProblem.zeroValue();
			}

			public EdgeFunctions<Unit,D,SootMethod,Constraint<String>> createEdgeFunctionsFactory() {
				return new IFDSEdgeFunctions(interproceduralCFG());
			}

			public JoinLattice<Constraint<String>> createJoinLattice() {
				return new JoinLattice<Constraint<String>>() {

					public Constraint<String> topElement() {
						return Constraint.falseValue();
					}

					public Constraint<String> bottomElement() {
						return Constraint.trueValue();
					}

					public Constraint<String> join(Constraint<String> left, Constraint<String> right) {
						return left.or(right);
					}
				};
			}
			
			class IFDSEdgeFunctions implements EdgeFunctions<Unit,D,SootMethod,Constraint<String>> {
				private final FlowFunctions<Unit, D, SootMethod> zeroedFlowFunctions;
				private final ExtendedInterproceduralCFG icfg;
				
				public IFDSEdgeFunctions(ExtendedInterproceduralCFG icfg) {
					this.icfg = icfg;
					zeroedFlowFunctions = new ZeroedFlowFunctions<Unit, D, SootMethod>(ifdsProblem.flowFunctions(),ifdsProblem.zeroValue());
				}

				public EdgeFunction<Constraint<String>> getNormalEdgeFunction(Unit currStmt, D currNode, Unit succStmt, D succNode) {
					return buildFlowFunction(currStmt, succStmt, currNode, succNode, zeroedFlowFunctions.getNormalFlowFunction(currStmt, succStmt), false);
				}
			
				public EdgeFunction<Constraint<String>> getCallEdgeFunction(Unit callStmt, D srcNode, SootMethod destinationMethod,D destNode) {
					return buildFlowFunction(callStmt, destinationMethod, srcNode, destNode, zeroedFlowFunctions.getCallFlowFunction(callStmt, destinationMethod), true);
				}
			
				public EdgeFunction<Constraint<String>> getReturnEdgeFunction(Unit callSite, SootMethod calleeMethod, Unit exitStmt,D exitNode, Unit returnSite,D retNode) {
					return buildFlowFunction(exitStmt, returnSite, exitNode, retNode, zeroedFlowFunctions.getReturnFlowFunction(callSite, calleeMethod, exitStmt, returnSite), false);
				}
			
				public EdgeFunction<Constraint<String>> getCallToReturnEdgeFunction(Unit callSite, D callNode, Unit returnSite, D returnSideNode) {
					return buildFlowFunction(callSite, returnSite, callNode, returnSideNode, zeroedFlowFunctions.getCallToReturnFlowFunction(callSite, returnSite), false);
				}
			
				private EdgeFunction<Constraint<String>> buildFlowFunction(Unit src, Host successor, D srcNode, D tgtNode, FlowFunction<D> originalFlowFunction, boolean isCall) {
					boolean srcAnnotated = hasFeatureAnnotation(src);
					boolean succAnnotated = hasFeatureAnnotation(successor);

					if(!srcAnnotated && !(isCall && succAnnotated)) return EdgeIdentity.v();
					BitSet features = new BitSet();
					if(srcAnnotated)
						features.or(features(src));
					if(isCall && succAnnotated)
						features.or(features(successor));
					
					boolean isFallThroughEdge = false;
					if(successor instanceof Unit) {
						Unit succUnit = (Unit) successor;
						isFallThroughEdge = icfg.isFallThroughSuccessor(src,succUnit);
					}		
					
					boolean canFallThrough = !isCall && src.fallsThrough();
					
					Constraint<String> pos = originalFlowFunction.computeTargets(srcNode).contains(tgtNode) && !(isFallThroughEdge && !canFallThrough) ?
							Constraint.<String>make(features,true) :
							Constraint.<String>falseValue();
					
					Constraint<String> neg = srcNode==tgtNode && isFallThroughEdge ?
							Constraint.<String>make(features,false):
							Constraint.<String>falseValue();
							
					Constraint<String> lifted = pos.or(neg);
					
					if(useFMInEdgeComputations) {
						lifted = lifted.and(fmContext.getSimplifiedFMConstraint());
					}
												
					return new SPLFeatureFunction(lifted, fmContext);
				}
			
				
				private BitSet features(Host successor) {
					FeatureTag tag = (FeatureTag) successor.getTag(FeatureTag.FEAT_TAG_NAME);
					if(tag==null) {
						return new BitSet();
					} else {
						BitSet features = tag.getFeatureRep();
						return features;
					}
				}
			}

			public EdgeFunction<Constraint<String>> createAllTopFunction() {
				return new SPLFeatureFunction(Constraint.<String>falseValue(), fmContext);
			}	
		});
		this.FULL_FM_CONSTRAINT = fmContext.getFullFMConstraint();
	}

	private static boolean hasFeatureAnnotation(Host host) {
		return host.hasTag(FeatureTag.FEAT_TAG_NAME);
	}
	
	/**
	 * Returns the set of facts that hold at the given statement.
	 */
	public Set<D> ifdsResultsAt(Unit statement) {
		return resultsAt(statement).keySet();
	}
	
	public Constraint<String> resultAt(Unit stmt, D value) {
		return super.resultAt(stmt, value).and(FULL_FM_CONSTRAINT);	
	}
	
	@Override
	public Map<D, Constraint<String>> resultsAt(Unit stmt) {
		Map<D, Constraint<String>> resultsAt = super.resultsAt(stmt);
		Map<D, Constraint<String>> res = new HashMap<D, Constraint<String>>();
		for(Entry<D,Constraint<String>> entry: resultsAt.entrySet()) {
			res.put(entry.getKey(), entry.getValue().and(FULL_FM_CONSTRAINT));
		}
		return res;
	}

	static class WrappedFlowFunction<D> implements FlowFunction<D> {
		
		private FlowFunction<D> del;

		private WrappedFlowFunction(FlowFunction<D> del) {
			this.del = del;
		}

		@Override
		public Set<D> computeTargets(D source) {
			Set<D> targets = new HashSet<D>(del.computeTargets(source));
			targets.add(source);
			return targets;
		}
	}
}
