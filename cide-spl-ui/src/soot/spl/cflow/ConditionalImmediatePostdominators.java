package soot.spl.cflow;

import java.util.HashMap;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.util.StringNumberer;

public class ConditionalImmediatePostdominators<T,N> extends ConditionalPostdominators<T,N> {
	
	private Map<N,Map<N,Constraint<T>>> unitToImmediatePostDomToConstraint;
	
	public ConditionalImmediatePostdominators(LabeledDirectedGraph<N,Constraint<T>> cfg) {
		super(cfg);
	}
	
	public ConditionalImmediatePostdominators(LabeledDirectedGraph<N,Constraint<T>> cfg, StringNumberer labelNumberer) {
		super(cfg, labelNumberer);
	}
	

	protected void compute() {
		super.compute();

		//initialize map
		unitToImmediatePostDomToConstraint = new HashMap<N,Map<N,Constraint<T>>>();
		for(N u: cfg) {
			HashMap<N, Constraint<T>> newMap = new HashMap<N, Constraint<T>>();
			unitToImmediatePostDomToConstraint.put(u, newMap);
		}
		
		//compute constraints
		for(N n: cfg) {
			for(N x: cfg) {
				Constraint<T> pdomNX = isPostDominatorOf(n, x);
				
				Constraint<T> disjunction = Constraint.falseValue(); 
				for(N xPrime: cfg) {
					if(xPrime==x || xPrime==n) continue;
					
					Constraint<T> pdomNXPrime = isPostDominatorOf(n, xPrime);
					Constraint<T> pdomXPrimeX = isPostDominatorOf(xPrime, x);
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
	
	public void print() {
		print("immediately-postdominates", unitToImmediatePostDomToConstraint);
	}
	
	public void outputGraphViz(String prefix) {
		outputGraphViz(prefix, "ipdom", unitToImmediatePostDomToConstraint, false);
	}

	
}
