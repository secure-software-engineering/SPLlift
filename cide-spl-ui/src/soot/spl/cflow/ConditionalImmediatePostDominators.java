package soot.spl.cflow;

import java.util.HashMap;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;

public class ConditionalImmediatePostDominators<T,N extends Host> {
	
	protected ConditionalPostdominators<T,N> pdomAnalysis;
	
	private Map<N,Map<N,Constraint<T>>> unitToImmediatePostDomToConstraint = new HashMap<N,Map<N,Constraint<T>>>();
	
	public ConditionalImmediatePostDominators(ConditionalPostdominators<T, N> pdomAnalysis) {
		this.pdomAnalysis = pdomAnalysis;
		compute();
	}

	private void compute() {
		DirectedGraph<N> cfg = pdomAnalysis.getControlFlowGraph();
		for(N u: cfg) {
			HashMap<N, Constraint<T>> newMap = new HashMap<N, Constraint<T>>();
			unitToImmediatePostDomToConstraint.put(u, newMap);
		}
		
		for(N n: cfg) {
			for(N x: cfg) {
				Constraint<T> pdomNX = pdomAnalysis.isPostDominatorOf(n, x);
				
				Constraint<T> disjunction = Constraint.falseValue(); 
				for(N xPrime: cfg) {
					if(xPrime==n) continue;
					
					Constraint<T> pdomNXPrime = pdomAnalysis.isPostDominatorOf(n, xPrime);
					Constraint<T> pdomXPrimeX = pdomAnalysis.isPostDominatorOf(xPrime, x);
					Constraint<T> conjunction = pdomNXPrime.and(pdomXPrimeX);
					
					disjunction = disjunction.or(conjunction);
				}				
				
				Constraint<T> negation = disjunction.not();
				
				Constraint<T> result = pdomNX.and(negation);
				
				unitToImmediatePostDomToConstraint.get(n).put(x,result);
			}
		}		
	}
	
	public Constraint<T> isImmediatePostDominatorOf(N node, N potentialImmediatePostDominator) {
		Map<N, Constraint<T>> map = unitToImmediatePostDomToConstraint.get(node);
		if(map==null) return Constraint.<T>falseValue();
		return map.get(potentialImmediatePostDominator);
	}

}
