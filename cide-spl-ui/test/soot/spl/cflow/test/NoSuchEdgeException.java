package soot.spl.cflow.test;

/**
 * Exception that is thrown whenever one attempts to access an edge which does
 * not exist in the graph.
 * 
 * @author Steven Arzt
 */
public class NoSuchEdgeException extends RuntimeException {
	
	private static final long serialVersionUID = -2841767855844711801L;

	@SuppressWarnings("rawtypes")
	private final Edge edge;
	
	/**
	 * Creates a new instance of the {@link NoSuchEdgeException} class.
	 * @param edge The edge that was not found in the graph
	 */
	public NoSuchEdgeException(@SuppressWarnings("rawtypes") Edge edge) {
		super();
		this.edge = edge;
	}
	
	/**
	 * Gets the edge that was not found in the graph. 
	 * @return The edge that was not found in the graph.
	 */
	@SuppressWarnings("rawtypes")
	public Edge getEdge() {
		return edge;
	}

}
