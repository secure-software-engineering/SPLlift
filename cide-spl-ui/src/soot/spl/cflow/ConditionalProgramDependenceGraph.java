package soot.spl.cflow;

import java.util.HashMap;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.toolkits.graph.DirectedGraph;
import soot.util.StringNumberer;

public class ConditionalProgramDependenceGraph<T,N> {
	
	private ConditionalPostdominators<T, N> cpda;
	
	protected Map<N,Map<N,Constraint<T>>> unitToControlDependeeToConstraint;

	public ConditionalProgramDependenceGraph(LabeledDirectedGraph<N,Constraint<T>> cfg) {
		this(cfg, null);
	}
	
	public ConditionalProgramDependenceGraph(LabeledDirectedGraph<N,Constraint<T>> cfg, StringNumberer labelNumberer) {
		cpda = new ConditionalPostdominators<T,N>(cfg, labelNumberer);
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
				
				for(N b: cfg.getSuccsOf(a)) {
					Constraint<T> c = cpda.constraintOfEdge(a,b);					
					Constraint<T> notPDomAB = pdom(a,b).not();					
					Constraint<T> cAndNotPDomAB = c.and(notPDomAB);
					
					Constraint<T> innerDisjunction = Constraint.falseValue();
					for(N l: cpda) {
						Constraint<T> lca = leastCommonAncestor(a,b,l);
						Constraint<T> path = onPath(l, b, x);
						Constraint<T> lEqAEqX = l==a && x==a ? Constraint.<T>trueValue():Constraint.<T>falseValue();
						Constraint<T> innerMostDisjunction = path.or(lEqAEqX);
						Constraint<T> disjunct = lca.and(innerMostDisjunction);
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
	
	public void print() {		
		cpda.print("is-control-dependent-on", unitToControlDependeeToConstraint);
//		for(N n1: cpda) {
//			for(N n2: cpda) {
//				for(N n3: cpda) {					
//					Constraint<T> lca = leastCommonAncestor(n1, n2, n3);
//					if((n1==n2 && n2==n3) || lca.equals(Constraint.falseValue())) continue;
//					System.err.println("lca("+n1+","+n2+")="+n3+" "+cpda.toConditionString(lca));					
//				}
//			}
//		}
//		System.err.println("- - - - -");
//		for(N n1: cpda) {
//			for(N n2: cpda) {
//				for(N n3: cpda) {					
//					Constraint<T> lca = onPath(n1, n2, n3);
//					if((n1==n2 && n2==n3) || lca.equals(Constraint.falseValue())) continue;
//					System.err.println(n3+" is on path("+n1+","+n2+") "+cpda.toConditionString(lca));					
//				}
//			}
//		}
	}

}
