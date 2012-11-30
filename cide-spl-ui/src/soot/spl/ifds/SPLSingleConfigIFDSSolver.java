package soot.spl.ifds;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.IFDSTabulationProblem;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import heros.solver.IFDSSolver;

import java.util.BitSet;
import java.util.Set;


import soot.SootMethod;
import soot.Unit;
import soot.tagkit.Host;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class SPLSingleConfigIFDSSolver<D> extends IFDSSolver<Unit,D,SootMethod,ExtendedInterproceduralCFG> {

	/**
	 * Creates a solver for the given problem. The solver must then be started by calling
	 * {@link #solve()}.
	 * @param alloyFilePath 
	 * @param numFeaturesPresent 
	 */
	public SPLSingleConfigIFDSSolver(final IFDSTabulationProblem<Unit,D,SootMethod,ExtendedInterproceduralCFG> ifdsProblem, final BitSet enabledFeatures) {
		super(new IFDSTabulationProblem<Unit,D,SootMethod,ExtendedInterproceduralCFG>() {

			public FlowFunctions<Unit,D,SootMethod> flowFunctions() {
				return new FlowFunctions<Unit,D,SootMethod>() {

					@Override
					public FlowFunction<D> getNormalFlowFunction(Unit curr, Unit succ) {
						FlowFunction<D> original = ifdsProblem.flowFunctions().getNormalFlowFunction(curr, succ);
						return flowFunction(curr, succ, original, false);
					}

					@Override
					public FlowFunction<D> getCallFlowFunction(Unit callStmt, SootMethod destinationMethod) {
						FlowFunction<D> original = ifdsProblem.flowFunctions().getCallFlowFunction(callStmt, destinationMethod);
						return flowFunction(callStmt, destinationMethod, original, true);
					}

					@Override
					public FlowFunction<D> getReturnFlowFunction(Unit callSite, SootMethod calleeMethod, Unit exitStmt, Unit returnSite) {
						FlowFunction<D> original = ifdsProblem.flowFunctions().getReturnFlowFunction(callSite, calleeMethod, exitStmt, returnSite);
						return flowFunction(exitStmt, returnSite, original, false);
					}

					@Override
					public FlowFunction<D> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
						FlowFunction<D> original = ifdsProblem.flowFunctions().getCallToReturnFlowFunction(callSite, returnSite);
						return flowFunction(callSite, returnSite, original, false);
					}
					
					protected FlowFunction<D> flowFunction(Unit src, Host successor, FlowFunction<D> original, boolean isCall) {
						boolean srcAnnotated = hasFeatureAnnotation(src);
						boolean succAnnotated = hasFeatureAnnotation(successor);

						if(!srcAnnotated && !(isCall && succAnnotated)) return original;
						
						BitSet features = new BitSet();
						
						if(hasFeatureAnnotation(src))
							features.or(features(src));
						if(isCall && hasFeatureAnnotation(successor))
							features.or(features(successor));

						//check if features is a subset of enabledFeatures: features < enabledFeatures <=> features && !enabledFeatures == \empty
						features.andNot(enabledFeatures);
						boolean stmtEnabled = features.isEmpty();
						
						boolean isFallThroughEdge = false;
						if(successor instanceof Unit) {
							Unit succUnit = (Unit) successor;
							isFallThroughEdge = interproceduralCFG().isFallThroughSuccessor(src,succUnit);
						}		
						
						boolean canFallThrough = !isCall && src.fallsThrough();
						
						if(stmtEnabled) {
							if(isFallThroughEdge && !canFallThrough)
								return KillAll.v();
							else
								return original;							
						} else {
							if(isFallThroughEdge)
								return Identity.v();
							else
								return KillAll.v();
						}

					}
				};
			}

			public ExtendedInterproceduralCFG interproceduralCFG() {
				return new ExtendedInterproceduralCFG(ifdsProblem.interproceduralCFG());
			}

			public Set<Unit> initialSeeds() {
				return ifdsProblem.initialSeeds();
			}

			public D zeroValue() {
				return ifdsProblem.zeroValue();
			}
		});
	}

	private static boolean hasFeatureAnnotation(Host host) {
		return host.hasTag(FeatureTag.FEAT_TAG_NAME);
	}
	
	private static BitSet features(Host h) {
		FeatureTag tag = (FeatureTag) h.getTag(FeatureTag.FEAT_TAG_NAME);
		BitSet features = tag.getFeatureRep();
		return features;
	}
}
