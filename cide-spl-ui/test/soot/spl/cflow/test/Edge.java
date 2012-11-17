package soot.spl.cflow.test;

/**
 * An edge in a labeled directed graph.
 * @author Steven Arzt
 *
 * @param <N> The type of the node classes in the graph.
 * @param <L> The type of the label classes in the graph.
 */
public class Edge<N, L> {

	private final N sourceNode;
	private final N targetNode;
	private final L label;
	
	/**
	 * Creates a new instance of the {@link Edge} class without a label
	 * @param sourceNode The source node at which the edge starts
	 * @param targetNode The target node at which the edge ends
	 */
	public Edge(N sourceNode, N targetNode) {
		this(sourceNode, targetNode, null);
	}

	/**
	 * Creates a new instance of the {@link Edge} class with a label
	 * @param sourceNode The source node at which the edge starts
	 * @param targetNode The target node at which the edge ends
	 * @param label The label that shall be associated with the edge
	 */
	public Edge(N sourceNode, N targetNode, L label) {
		super();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.label = label;
	}
	
	/**
	 * Gets the source node of this edge
	 * @return The source node of this edge
	 */
	public N getSourceNode() {
		return this.sourceNode;
	}
	
	/**
	 * Gets the target node of this edge
	 * @return The target node of this edge
	 */
	public N getTargetNode() {
		return this.targetNode;
	}
	
	/**
	 * Gets the label associated with this node, if any. If no label exists for
	 * this edge, null is returned.
	 * @return The label associated with this edge.
	 */
	public L getLabel() {
		return this.label;
	}
	
	@Override
	public String toString() {
		return this.sourceNode + " -> " + this.targetNode
				+ (this.label.toString().isEmpty() ? "" : " (" + this.label + ")");
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object another) {
		if (super.equals(another))
			return true;
		if (another == null)
			return false;
		if (!(another instanceof Edge))
			return false;
		Edge anotherEdge = (Edge) another;
		return this.sourceNode.equals(anotherEdge.sourceNode)
				&& this.targetNode.equals(anotherEdge.targetNode);
	}
	
	@Override
	public int hashCode() {
		return sourceNode.hashCode() + 31 * targetNode.hashCode();
	}

}
