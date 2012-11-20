package soot.spl.cflow;

import graphviz.GraphViz;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.spl.ifds.Constraint;
import soot.util.StringNumberer;

public class ConditionalPostdominators<T,N> implements Iterable<N>{
	
	protected final LabeledDirectedGraph<N,Constraint<T>> cfg;
	
	protected Map<N,Map<N,Constraint<T>>> unitToPostDomToConstraint;

	protected final StringNumberer labelNumberer;

	public ConditionalPostdominators(LabeledDirectedGraph<N,Constraint<T>> cfg) {
		this(cfg, null);
	}
	
	public ConditionalPostdominators(LabeledDirectedGraph<N,Constraint<T>> cfg, StringNumberer labelNumberer) {
		this.cfg = cfg;
		this.labelNumberer = labelNumberer;
		compute();
	}

	public String toString(Constraint<T> constraint) {
		if(labelNumberer==null) {
			return constraint.toString();
		} else {			
			return constraint.toString(labelNumberer);
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
		
//		System.err.println(unitToPostDomToConstraint);
		
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
//		System.err.println("update: "+n+" "+x+" "+val);
		return !prev.equals(val);
	}

	protected Constraint<T> constraintOfEdge(N n, N succ) {
		return cfg.getLabel(n, succ);
	}
	
	public Constraint<T> isPostDominatorOf(N node, N potentialPostDominator) {
		Map<N, Constraint<T>> map = unitToPostDomToConstraint.get(node);
		if(map==null) return Constraint.<T>falseValue();
		return map.get(potentialPostDominator);
	}
	
	public void print() {
		print("postdominates", unitToPostDomToConstraint);
	}
	
	protected void print(String verb, Map<N,Map<N,Constraint<T>>> map) {
		System.err.println("================================");
		for(N n: cfg) {
			System.err.print(n+"  ");
			for(N succ: cfg.getSuccsOf(n)) {
				System.err.print(toString(cfg.getLabel(n, succ))+"->"+succ+"  ");
			}
			System.err.println();
		}
		System.err.println("- - - - - - - - - - - - - - - - ");

		for(N n: cfg) {
			for(N n2: cfg) {
				Constraint<T> constraint = map.get(n).get(n2);
				if(n==n2 || constraint.equals(Constraint.falseValue())) continue;
				System.err.print(n2+" "+verb+" "+n);				
				System.err.println(" "+toConditionString(constraint));				
			}
		}
		System.err.println("================================");
	}
	
	public void outputGraphViz(String prefix) {
		outputGraphViz(prefix, "pdom", unitToPostDomToConstraint, false);
	}
	
	protected void outputGraphViz(String prefix, String fileName, Map<N,Map<N,Constraint<T>>> map, boolean reverseDirection) {
	      GraphViz gv = new GraphViz();
	      gv.addln(gv.start_graph());
	      for(N n1: cfg) {
		      for(N n2: cfg) {
		    	  Constraint<T> constraint = map.get(n1).get(n2);
		    	  if(n1==n2 || constraint.equals(Constraint.falseValue())) continue;
		    	  if(reverseDirection) {
		    		  gv.addln(n1+" -> "+n2+" [label=\""+toString(constraint)+"\"];");
		    	  } else {
		    		  gv.addln(n2+" -> "+n1+" [label=\""+toString(constraint)+"\"];");
		    	  }
		      }
	      }
	      gv.addln(gv.end_graph());
	      
	      String type = "pdf";
	      File out = new File("/tmp/"+prefix+"-"+fileName+"." + type);   // Linux
	      gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}

	public String toConditionString(Constraint<T> constraint) {
		String res;
		if(constraint.equals(Constraint.falseValue())){
			res="never";
		} else if(constraint.equals(Constraint.trueValue())) {
			res="always";
		} else {
			res="if "+toString(constraint);
		}
		return res;
	}

	@Override
	public Iterator<N> iterator() {
		return cfg.iterator();
	}
	
	public LabeledDirectedGraph<N,Constraint<T>> getControlFlowGraph() {
		return cfg;
	}
}
