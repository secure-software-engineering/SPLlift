package soot.spl.cflow;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import soot.Unit;
import soot.spl.ifds.Constraint;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class ConditionalPostdominators<T> {
	
	private final DirectedGraph<Unit> cfg;
	
	private Map<Unit,Map<Unit,Constraint<T>>> unitToPostDomToConstraint = new HashMap<Unit,Map<Unit,Constraint<T>>>();

	public ConditionalPostdominators(DirectedGraph<Unit> cfg) {
		this.cfg = cfg;
		compute();
	}

	private void compute() {
		//initialize map
		for(Unit u: cfg) {
			HashMap<Unit, Constraint<T>> newMap = new HashMap<Unit, Constraint<T>>();
			unitToPostDomToConstraint.put(u, newMap);
			for(Unit u2: cfg) {
				Constraint<T> c = (cfg.getTails().contains(u) && u!=u2) ?
					Constraint.<T>falseValue() : Constraint.<T>trueValue();
				newMap.put(u2, c);					
			}
		}
		
		boolean changed = false;		
		do {
			for(Unit n: cfg) {
				if(cfg.getTails().contains(n)) continue;
				for(Unit x: cfg) {
					if(n==x) {
						changed = updateConstraint(n, x, Constraint.<T>trueValue());
					} else {
						//FIXME compute different constraints for target and fallthrough
						for(Unit s: cfg.getSuccsOf(n)) {
							Constraint<T> c = constraintOfEdge(s);
							Constraint<T> pdomSX = unitToPostDomToConstraint.get(s).get(x);
							changed = updateConstraint(n, x, c.implies(pdomSX));							
						}
					}
				}
			}
		} while (changed);
	}

	private boolean updateConstraint(Unit n, Unit x, Constraint<T> val) {
		Map<Unit, Constraint<T>> map = unitToPostDomToConstraint.get(n);
		Constraint<T> prev = map.put(x, val);
		return prev!=val;
	}

	private Constraint<T> constraintOfEdge(Unit u) {
		FeatureTag tag = (FeatureTag) u.getTag(FeatureTag.FEAT_TAG_NAME);
		if(tag==null) {
			return Constraint.trueValue();
		} else {
			BitSet features = tag.getFeatureRep();
			return Constraint.make(features, true);
		}
	}
	
	public Constraint<T> isPostDominatorOf(Unit node, Unit potentialPostDominator) {
		Map<Unit, Constraint<T>> map = unitToPostDomToConstraint.get(node);
		if(map==null) return Constraint.<T>falseValue();
		return map.get(potentialPostDominator);
	}
	
	
}
