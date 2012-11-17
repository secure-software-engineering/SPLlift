package soot.spl.cflow.test;

/**
 * Common interface for all nodes in a labeled directed graph.
 *
 * @author Steven Arzt
 *
 */
public interface Node {
	
	/**
	 * Gets whether this node is a start node of the graph.
	 * @return True if this node is a start node of the graph, otherwise false.
	 */
	public boolean isStartNode();

	/**
	 * Gets whether this node is an exit node of the graph.
	 * @return True if this node is an exit node of the graph, otherwise false.
	 */
	public boolean isExitNode();

}
