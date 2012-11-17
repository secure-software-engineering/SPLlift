package soot.spl.cflow;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class ConditionalPostdominators<T,N extends Host> implements Iterable<N>{
	
	protected final DirectedGraph<N> cfg;
	
	protected Map<N,Map<N,Constraint<T>>> unitToPostDomToConstraint;

	public ConditionalPostdominators(DirectedGraph<N> cfg) {
		this(cfg, true);
	}
	
	public ConditionalPostdominators(DirectedGraph<N> cfg, boolean wellformednessCheck) {
		this.cfg = cfg;
		if(wellformednessCheck)
			checkWellformedness();
		compute();
	}

	private void checkWellformedness() {
		for(N n: cfg) {
			Constraint<T> disjunction = Constraint.falseValue();
			for(N succ: cfg.getSuccsOf(n)) {
				disjunction = disjunction.or(constraintOfEdge(n,succ));
			}
			if(!disjunction.equals(Constraint.trueValue())) {
				throw new RuntimeException("Conditional CFG is not well formed! At Stmt "+n+" we get constraint "+disjunction);
			}
		}
	}

	protected void compute() {
		//initialize map
		unitToPostDomToConstraint = new HashMap<N,Map<N,Constraint<T>>>();
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
							Constraint<T> c = constraintOfEdge(n,s);
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
		return !prev.equals(val);
	}

	protected Constraint<T> constraintOfEdge(N n, N succ) {
		return constraintOfNode(n).and(constraintOfNode(succ));
	}

	private Constraint<T> constraintOfNode(N n) {
		FeatureTag tag = (FeatureTag) n.getTag(FeatureTag.FEAT_TAG_NAME);
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
			System.err.println(i+" "+n+"   "+constraintOfNode(n));
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

	@Override
	public Iterator<N> iterator() {
		return cfg.iterator();
	}
	
	public DirectedGraph<N> getControlFlowGraph() {
		return cfg;
	}
}
