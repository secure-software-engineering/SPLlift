package soot.spl.cflow;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class ConditionalPostdominators<T,N extends Host> {
	
	private final DirectedGraph<N> cfg;
	
	private Map<N,Map<N,Constraint<T>>> unitToPostDomToConstraint = new HashMap<N,Map<N,Constraint<T>>>();

	public ConditionalPostdominators(DirectedGraph<N> cfg) {
		this.cfg = cfg;
		compute();
	}

	private void compute() {
		//initialize map
		for(N u: cfg) {
			HashMap<N, Constraint<T>> newMap = new HashMap<N, Constraint<T>>();
			unitToPostDomToConstraint.put(u, newMap);
			for(N u2: cfg) {
				Constraint<T> c = (cfg.getTails().contains(u) && u!=u2) ?
					Constraint.<T>falseValue() : Constraint.<T>trueValue();
				newMap.put(u2, c);					
			}
		}
		
		System.err.println(unitToPostDomToConstraint);
		
		boolean changed;		
		do {
			changed = false;
			for(N n: cfg) {
				if(cfg.getTails().contains(n)) continue;
				for(N x: cfg) {
					if(n==x) {
						changed |= updateConstraint(n, x, Constraint.<T>trueValue());
					} else {
						//FIXME compute different constraints for target and fallthrough
						Constraint<T> conjunction = Constraint.<T>trueValue();
						for(N s: cfg.getSuccsOf(n)) {
							Constraint<T> c = constraintOfEdge(s);
							Constraint<T> pdomSX = unitToPostDomToConstraint.get(s).get(x);
							Constraint<T> conjunct = c.implies(pdomSX);
							conjunction = conjunction.and(conjunct);
						}
						changed |= updateConstraint(n, x, conjunction);							
					}
				}
			}
		} while (changed);
	}

	private boolean updateConstraint(N n, N x, Constraint<T> val) {
		Map<N, Constraint<T>> map = unitToPostDomToConstraint.get(n);
		Constraint<T> prev = map.put(x, val);
		System.err.println("update: "+n+" "+x+" "+val);
		return prev!=val;
	}

	private Constraint<T> constraintOfEdge(N u) {
		FeatureTag tag = (FeatureTag) u.getTag(FeatureTag.FEAT_TAG_NAME);
		if(tag==null) {
			return Constraint.trueValue();
		} else {
			BitSet features = tag.getFeatureRep();
			return Constraint.make(features, true);
		}
	}
	
	public Constraint<T> isPostDominatorOf(N node, N potentialPostDominator) {
		Map<N, Constraint<T>> map = unitToPostDomToConstraint.get(node);
		if(map==null) return Constraint.<T>falseValue();
		return map.get(potentialPostDominator);
	}
	
	public String print() {
		int i=1;
		for(N n: cfg) {
			System.err.println(i+" "+n+"   "+constraintOfEdge(n));
			i++;
		}

		i=1;
		for(N n: cfg) {
			int j=1;
			for(N n2: cfg) {
				System.err.println("pdom("+i+","+j+")="+unitToPostDomToConstraint.get(n).get(n2));
				j++;
			}
			i++;
		}

		return super.toString();
	}
	
	
}
