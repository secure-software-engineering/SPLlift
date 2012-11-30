package soot.spl.cflow.baseline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.spl.cflow.LabeledDirectedGraph;
import soot.spl.ifds.Constraint;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.util.StringNumberer;

public class TraditionalControlDependenceGraph<T,N> {
	
	protected final MHGPostDominatorsFinder<N> mhgPostDominatorsFinder;
	
	protected final Map<N,Set<N>> unitToPostDom = new HashMap<N, Set<N>>();

	protected final FilteredCFG graph;
	
	public TraditionalControlDependenceGraph(LabeledDirectedGraph<N,Constraint<T>> cfg, Constraint<T> enabledFeatures) {
		this(cfg, enabledFeatures, null);
	}
	
	public TraditionalControlDependenceGraph(LabeledDirectedGraph<N,Constraint<T>> cfg, Constraint<T> enabledFeatures, StringNumberer labelNumberer) {
		graph = new FilteredCFG(cfg,enabledFeatures);
		mhgPostDominatorsFinder = new MHGPostDominatorsFinder<N>(graph);
		compute();
	}

	private void compute() {
		for(N n: graph) {
			unitToPostDom.put(n, new HashSet<N>());
		}
		for(N a: graph) {
			for(N p: graph) {
				for(N b: graph.getSuccsOf(a)) {
					if(!pdom(a, b) && pdom(b,p) && !pdom(a,p)) {
						unitToPostDom.get(a).add(p);
					}
				}
			}
		}		
	}

	private boolean pdom(N a, N b) {
		return mhgPostDominatorsFinder.isDominatedBy(a, b);
	}
	
	protected class FilteredCFG implements DirectedGraph<N> {
		
		protected final LabeledDirectedGraph<N,Constraint<T>> delegate;
		protected final Constraint<T> enabledFeatures;
		protected Map<N,List<N>> succs = new HashMap<N,List<N>>(); 
		protected Map<N,List<N>> preds = new HashMap<N,List<N>>(); 
		
		public FilteredCFG(LabeledDirectedGraph<N,Constraint<T>> delegate, Constraint<T> enabledFeatures) {
			this.delegate = delegate;
			this.enabledFeatures = enabledFeatures;
			Set<N> worklist = new HashSet<N>();
			worklist.addAll(delegate.getHeads());
			Set<N> reachableNodes = new HashSet<N>(); 
			while(!worklist.isEmpty()) {
				//pop an element
				Iterator<N> iter = worklist.iterator();
				N n = iter.next();
				iter.remove();
				reachableNodes.add(n);
				for(N succ: delegate.getSuccsOf(n)) {
					Constraint<T> label = delegate.getLabel(n, succ);
					if(!enabledFeatures.and(label).equals(Constraint.falseValue())) {
						if(!reachableNodes.contains(succ))
							worklist.add(succ);
						
						List<N> succList = succs.get(n);
						if(succList==null) {
							succList = new ArrayList<N>();
							succs.put(n, succList);
						}
						succList.add(succ);
						
						List<N> predList = preds.get(n);
						if(predList==null) {
							predList = new ArrayList<N>();
							preds.put(n, predList);
						}
						predList.add(n);
					}
				}
			}
		}

		@Override
		public List<N> getHeads() {
			return delegate.getHeads();
		}

		@Override
		public List<N> getTails() {
			return delegate.getTails();
		}

		@Override
		public List<N> getPredsOf(N s) {
			List<N> list = preds.get(s);
			if(list==null) return Collections.emptyList();
			else return list;
		}

		@Override
		public List<N> getSuccsOf(N s) {
			List<N> list = succs.get(s);
			if(list==null) return Collections.emptyList();
			else return list;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public Iterator<N> iterator() {
			return delegate.iterator();
		}
	}


}
