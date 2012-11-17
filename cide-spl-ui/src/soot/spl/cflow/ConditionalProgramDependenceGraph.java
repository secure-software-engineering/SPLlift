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
		
		DirectedGraph<N> cfg = cpda.getControlFlowGraph();
		
		for(N a: cpda) {
			for(N x: cpda) {
				Constraint<T> disjunction = Constraint.falseValue();
				
				for(N b: cfg.getSuccsOf(x)) {
					Constraint<T> c = cpda.constraintOfEdge(x,b);					
					Constraint<T> notPDomAB = pdom(a,b).not();					
					Constraint<T> cAndNotPDomAB = c.and(notPDomAB);
					
					Constraint<T> innerDisjunction = Constraint.falseValue();
					for(N l: cpda) {
						Constraint<T> lca = leastCommonAncestor(a,b,l);
						Constraint<T> path = onPath(l, b, x);
						Constraint<T> disjunct = lca.and(path);
						innerDisjunction = innerDisjunction.or(disjunct);
					}					
					
					disjunction = disjunction.or(cAndNotPDomAB.and(innerDisjunction));
				}
				
				unitToControlDependeeToConstraint.get(a).put(x, disjunction);
			}
		}
	}
	
	private Constraint<T> leastCommonAncestor(N node1, N node2, N potentialCommonAncestor) {
		Constraint<T> ca = commonAncestor(node1, node2, potentialCommonAncestor);
		Constraint<T> conjunction = Constraint.trueValue();
		for(N y: cpda){
			Constraint<T> conjunct = commonAncestor(node1, node2, y).implies(pdom(potentialCommonAncestor, y));
			conjunction = conjunction.and(conjunct);
		}
		
		return ca.and(conjunction);
	}

	private Constraint<T> commonAncestor(N a, N b, N n) {
		return pdom(a, n).
				and(pdom(b, n));
	}

	private Constraint<T> pdom(N a, N b) {
		return cpda.isPostDominatorOf(a, b);
	}

	private Constraint<T> onPath(N a, N b, N n) {
		Constraint<T> aNotEqualsN = a!=n ? Constraint.<T>trueValue() : Constraint.<T>falseValue();
		return pdom(n, a).
				and(pdom(b, n)).
				and(aNotEqualsN);
	}

}
