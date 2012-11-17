package soot.spl.cflow.test;

import java.util.HashMap;
import java.util.Map;

import soot.spl.cflow.LabeledDirectedGraph;

/**
 * Simple directed graph builder that reads in a structured text file or string
 * and creates a labeled directed graph from it.
 * 
 * @author Steven Arzt
 */
public class SimpleGraphBuilder {
	
	/**
	 * Builds a labeled directed graph from a well-formatted input string. The
	 * input string must have one edge per line. Edges must be of the format
	 * n1 -> n2 (l) where n1 and n2 are nodes and l is a label.
	 * @param input The well-formatted input string
	 * @return The labeled directed graph constructed from the input string
	 */
	public LabeledDirectedGraph<Node, String> buildGraphFromString(String input) {
		LabeledDirectedGraphImpl<Node, String> graph = new LabeledDirectedGraphImpl<Node, String>();
		Map<String, SimpleNode> nodeCache = new HashMap<String, SimpleNode>();
		
		int lineNum = 1;
		String[] lines = input.split("\n");
		for (String line : lines) {
			line = line.trim();
			line = line.replaceAll("#\\S+", "");	// throw out the comments
			if (line.length() == 0)	// throw out blank lines
				continue;
			
			if (line.matches("STARTNODE\\s+[\\w ]+")) {
				String nodeName = line.replaceAll("STARTNODE\\s+", "");
				if (nodeCache.containsKey(nodeName))
					nodeCache.get(nodeName).setStartNode(true);
				else
					nodeCache.put(nodeName, new SimpleNode(nodeName, true, false));
				continue;
			}
				
			if (line.matches("EXITNODE\\s+[\\w ]+")) {
				String nodeName = line.replaceAll("EXITNODE\\s+", "");
				if (nodeCache.containsKey(nodeName))
					nodeCache.get(nodeName).setExitNode(true);
				else
					nodeCache.put(nodeName, new SimpleNode(nodeName, false, true));
				continue;
			}
			
			if (!line.matches("[\\w ]+(\\s*->\\s*)[\\w ]+(\\([\\w ]+\\))?")) {
				System.err.println("Syntax error on line " + lineNum + ", resulting graph my be incomplete. "
						+ "Offending line: " + line);
				continue;				
			}
			
			String[] parts = line.split("(\\s*->\\s*)");
			if (parts.length < 2) {
				System.err.println("Could not split base elements on line " + lineNum);
				continue;
			}
			
			String n1 = parts[0].trim();
			String n2 = parts[1].trim();
			String label = "";
			if (parts[1].matches("[\\w ]+\\([\\w ]+\\)")) {
				String[] lbParts = parts[1].split("\\(|\\)");
				if (lbParts.length < 1) {
					System.err.println("Could not split label on line " + lineNum);
					continue;
				}
				n2 = lbParts[0].trim();
				label = lbParts[1].trim();
			}
			
			SimpleNode node1 = nodeCache.get(n1);
			if (node1 == null) {
				node1 = new SimpleNode(n1);
				nodeCache.put(n1, node1);
			}
			SimpleNode node2 = nodeCache.get(n2);
			if (node2 == null) {
				node2 = new SimpleNode(n2);
				nodeCache.put(n2, node2);
			}
			
			Edge<Node,String> edge = new Edge<Node, String>(node1, node2, label);
			graph.addEdge(edge);
			System.out.println(edge);
			
			lineNum++;
		}
		
		return graph;
	}

}
