package soot.spl.cflow;

import soot.toolkits.graph.DirectedGraph;

public interface LabeledDirectedGraph<N,L> extends DirectedGraph<N> {

	public L getLabel(N source, N target);

}
