package soot.spl.cflow.test;

/**
 * Labeled node in a graph
 * 
 * @author Steven Arzt
 */
public class SimpleNode implements Node {

	private final String nodeName;
	private boolean isStartNode;
	private boolean isEndNode;
	
	/**
	 * Creates a new instance of the {@link SimpleNode} that is neither a start
	 * node nor an end node
	 * @param nodeName The name to be assigned to this node
	 */
	public SimpleNode(String nodeName) {
		this(nodeName, false, false);
	}
	
	/**
	 * Creates a new instance of the {@link SimpleNode}
	 * @param nodeName The name to be assigned to this node
	 * @param startNode True if this node is a start node, otherwise false
	 * @param endNode True if this node is an end node, otherwise false
	 */
	public SimpleNode(String nodeName, boolean startNode, boolean endNode) {
		this.nodeName = nodeName;
		this.isStartNode = startNode;
		this.isEndNode = endNode;
	}

	/**
	 * Gets the name associated with this graph node.
	 * @return The name associated with this graph node
	 */
	public String getNodeName() {
		return this.nodeName;
	}
	
	@Override
	public String toString() {
		return "<" + this.nodeName + ">";
	}

	@Override
	public boolean isStartNode() {
		return this.isStartNode;
	}
	
	/**
	 * Sets whether this is a start node of the labeled directed graph
	 * @param isStartNode True if this is a start node of the labeled directed
	 * graph, otherwise false
	 */
	public void setStartNode(boolean isStartNode) {
		this.isStartNode = isStartNode;
	}

	@Override
	public boolean isExitNode() {
		return this.isEndNode;
	}
	
	/**
	 * Sets whether this is an exit node of the labeled directed graph
	 * @param isExitNode True if this is an exit node of the labeled directed
	 * graph, otherwise false
	 */
	public void setExitNode(boolean isExitNode) {
		this.isEndNode = isExitNode;
	}

}
