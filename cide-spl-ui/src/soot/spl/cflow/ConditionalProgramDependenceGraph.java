package soot.spl.cflow;

import java.util.HashMap;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.tagkit.Host;
import soot.toolkits.graph.DirectedGraph;

public class ConditionalProgramDependenceGraph<T,N extends Host> {
	
	private ConditionalPostdominators<T, N> cpda;
	
	protected Map<N,Map<N,Constraint<T>>> unitToControlDependeeToConstraint;


	public ConditionalProgramDependenceGraph(DirectedGraph<N> cfg) {
		cpda = new ConditionalPostdominators<T,N>(cfg);
		compute();
	}

	protected void compute() {
		//initialize map
		unitToControlDependeeToConstraint = new HashMap<N,Map<N,Constraint<T>>>();
		for(N a: cpda) {
			HashMap<N, Constraint<T>> newMap = new HashMap<N, Constraint<T>>();
			unitToControlDependeeToConstraint.put(a, newMap);
		}
		
		for(N x: cpda) {

		}
	}
	
	private Constraint<T> leastCommonAncestor(N node1, N node2, N potentialCommonAncestor) {
		Constraint<T> ca = commonAncestor(node1, node2, potentialCommonAncestor);
		Constraint<T> conjunction = Constraint.trueValue();
		for(N y: cpda){
			Constraint<T> conjunct = commonAncestor(node1, node2, y).implies(cpda.isPostDominatorOf(potentialCommonAncestor,y));
			conjunction = conjunction.and(conjunct);
		}
		
		return ca.and(conjunction);
	}

	private Constraint<T> commonAncestor(N node1, N node2, N potentialCommonAncestor) {
		return cpda.isPostDominatorOf(node1, potentialCommonAncestor).
				and(cpda.isPostDominatorOf(node2, potentialCommonAncestor));
	}

}
