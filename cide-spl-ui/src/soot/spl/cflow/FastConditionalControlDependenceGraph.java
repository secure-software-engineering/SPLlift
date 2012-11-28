package soot.spl.cflow;

import java.util.HashMap;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.toolkits.graph.DirectedGraph;
import soot.util.StringNumberer;

/*
 * Computes the Conditional CDG without the need for the relations <i>path</i> and <i>lca</i>. 
 */
public class FastConditionalControlDependenceGraph<T,N> {
	
	private ConditionalPostdominators<T, N> cpda;
	
	protected Map<N,Map<N,Constraint<T>>> unitToControlDependeeToConstraint;

	public FastConditionalControlDependenceGraph(LabeledDirectedGraph<N,Constraint<T>> cfg) {
		this(cfg, null);
	}
	
	public FastConditionalControlDependenceGraph(LabeledDirectedGraph<N,Constraint<T>> cfg, StringNumberer labelNumberer) {
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
			for(N p: cpda) {
				Constraint<T> disjunction = Constraint.falseValue();
				
				for(N b: cfg.getSuccsOf(a)) {
					Constraint<T> c = cpda.constraintOfEdge(a, b);
					Constraint<T> disjunct = c.and(pdom(a,b).not().and(pdom(b,p).and(pdom(a,p).not())));
					
					disjunction = disjunction.or(disjunct);
				}
				
				unitToControlDependeeToConstraint.get(a).put(p, disjunction);
			}
		}
	}

	private Constraint<T> pdom(N a, N b) {
		return cpda.isPostDominatorOf(a, b);
	}

	public void print() {		
		cpda.print("is-control-dependent-on", unitToControlDependeeToConstraint);
	}
	
	public void outputGraphViz(String prefix) {
		cpda.outputGraphViz(prefix, "fast-pdg", unitToControlDependeeToConstraint, true);
	}
	
	public Constraint<T> isDependentOn(N node, N potentialBranch) {
		return unitToControlDependeeToConstraint.get(node).get(potentialBranch);
	}


}
