package soot.spl.cflow.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.spl.cflow.LabeledDirectedGraph;
import soot.toolkits.graph.MutableDirectedGraph;

/**
 * Simple implementation of the {@link LabeledDirectedGraph} interface.
 *
 * @author Steven Arzt
 */
public class LabeledDirectedGraphImpl<N extends Node,L>
		implements LabeledDirectedGraph<N,L>, MutableDirectedGraph<N> {
	
	private final static int DEFAULT_SIZE = 42;
	
	private final Map<N,Set<Edge<N,L>>> nodesToEdges;
	
	/**
	 * Creates a new instance of the {@link LabeledDirectedGraphImpl} class
	 * without any initial root node.
	 */
	public LabeledDirectedGraphImpl() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Creates a new instance of the {@link LabeledDirectedGraphImpl} class
	 * without any initial root node.
	 * @param size The startup size for the internal data objects - i.e. the
	 * anticipated number of nodes in the graph.
	 */
	public LabeledDirectedGraphImpl(int size) {
		this.nodesToEdges = new HashMap<N,Set<Edge<N,L>>>(size);
	}

	/**
	 * Creates a new instance of the {@link LabeledDirectedGraphImpl} class
	 * with a single root node for the graph.
	 * @param rootNode The graph's single root node
	 */
	public LabeledDirectedGraphImpl(N rootNode) {
		this(rootNode, DEFAULT_SIZE);
	}

	/**
	 * Creates a new instance of the {@link LabeledDirectedGraphImpl} class
	 * with a single root node for the graph.
	 * @param rootNode The graph's single root node
	 * @param size The startup size for the internal data objects - i.e. the
	 * anticipated number of nodes in the graph.
	 */
	public LabeledDirectedGraphImpl(N rootNode, int size) {
		this.nodesToEdges = new HashMap<N,Set<Edge<N,L>>>(size);
		this.addNode(rootNode);
	}

	/**
	 * Creates a new instance of the {@link LabeledDirectedGraphImpl} class
	 * for an exploded graph with multiple root nodes.
	 * @param rootNodes The list of root nodes for the graph.
	 */
	public LabeledDirectedGraphImpl(List<N> rootNodes) {
		this(rootNodes, DEFAULT_SIZE);
	}
	
	/**
	 * Creates a new instance of the {@link LabeledDirectedGraphImpl} class
	 * for an exploded graph with multiple root nodes.
	 * @param rootNodes The list of root nodes for the graph.
	 * @param size The startup size for the internal data objects - i.e. the
	 * anticipated number of nodes in the graph.
	 */
	public LabeledDirectedGraphImpl(List<N> rootNodes, int size) {
		this.nodesToEdges = new HashMap<N,Set<Edge<N,L>>>(size);
		for (N n : rootNodes)
			this.addNode(n);
	}
	
	@Override
	public List<N> getHeads() {
		List<N> startNodes = new ArrayList<N>();
		for (N n : this.nodesToEdges.keySet())
			if (n.isStartNode())
				startNodes.add(n);
		return startNodes;
	}

	@Override
	public List<N> getTails() {
		List<N> exitNodes = new ArrayList<N>();
		for (N n : this.nodesToEdges.keySet())
			if (n.isExitNode())
				exitNodes.add(n);
		return exitNodes;
	}

	@Override
	public List<N> getPredsOf(N s) {
		List<N> preds = new ArrayList<N>();
		for (Edge<N,L> e : this.nodesToEdges.get(s))
			if (e.getTargetNode() == s)
				preds.add(e.getSourceNode());
		return preds;
	}

	@Override
	public List<N> getSuccsOf(N s) {
		List<N> succs = new ArrayList<N>();
		for (Edge<N,L> e : this.nodesToEdges.get(s))
			if (e.getSourceNode() == s)
				succs.add(e.getTargetNode());
		return succs;
	}

	@Override
	public int size() {
		return this.nodesToEdges.size();
	}

	@Override
	public Iterator<N> iterator() {
		return this.nodesToEdges.keySet().iterator();
	}

	private Edge<N, L> findEdge(N source, N target) {
		if (this.containsNode(source) && this.containsNode(target))
			for (Edge<N,L> e : this.nodesToEdges.get(source))
				if ((e.getSourceNode() == source && e.getTargetNode() == target)
						|| (e.getSourceNode() == target && e.getTargetNode() == source))
					return e;
		return null;
	}

	public Edge<N, L> getEdge(N source, N target) throws NoSuchEdgeException {
		Edge<N,L> edge = findEdge(source, target);
		if (edge != null)
			return edge;
		throw new NoSuchEdgeException(new Edge<N,L>(source, target));
	}

	@Override
	public boolean containsEdge(N source, N target) {
		return this.findEdge(source, target) != null;
	}

	public void addEdge(Edge<N,L> edge) {
		this.addNode(edge.getSourceNode());
		this.addNode(edge.getTargetNode());
		
		this.nodesToEdges.get(edge.getSourceNode()).add(edge);
		this.nodesToEdges.get(edge.getTargetNode()).add(edge);
		
	}
	
	@Override
	public void addEdge(N from, N to) {
		this.addEdge(new Edge<N,L>(from, to));
	}

	@Override
	public void removeEdge(N from, N to) {
		if (!containsNode(from) || !containsNode(to))
			return;
		
		Edge<N,L> edge = new Edge<N,L>(from, to);
		this.nodesToEdges.get(from).remove(edge);
		this.nodesToEdges.get(to).remove(edge);
	}

	@Override
	public List<N> getNodes() {
		return new ArrayList<N>(this.nodesToEdges.keySet());
	}

	@Override
	public void addNode(N node) {
		if (!this.nodesToEdges.containsKey(node))
			this.nodesToEdges.put(node, new HashSet<Edge<N,L>>());
	}

	@Override
	public void removeNode(N node) {
		this.nodesToEdges.remove(node);
	}

	@Override
	public boolean containsNode(N node) {
		return this.nodesToEdges.containsKey(node);
	}

	@Override
	public L getLabel(N source, N target) {
		return getEdge(source, target).getLabel();		
	}

}
