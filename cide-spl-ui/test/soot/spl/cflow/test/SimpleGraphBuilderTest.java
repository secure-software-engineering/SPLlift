package soot.spl.cflow.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import soot.spl.cflow.LabeledDirectedGraph;

/**
 * Test class for the {@link SimpleGraphBuilder} class
 *
 * @author Steven Arzt
 *
 */
public class SimpleGraphBuilderTest {

	@Test
	public void test() {
		String s = "STARTNODE n1\n"
				+ "STARTNODE n6\n"
				+ "\n"	// optional blank line
				+ "EXITNODE n5\n"
				+ "EXITNODE n8\n"
				+ "\n"	// optional blank line
				+ "n1 -> n2 #comment\n"
				+ "n2 -> n3\n"
				+ "n2 -> n4\n"
				+ "n3 -> n4 (blue)\n"
				+ "n4 -> n5\n"
				+ "n6 -> n7\n"
				+ "n7 -> n6\n"
				+ "n7 -> n8";
		SimpleGraphBuilder builder = new SimpleGraphBuilder();
		LabeledDirectedGraph<Node, String> graph = builder.buildGraphFromString(s);
		Assert.assertNotNull("Returned graph was null", graph);
		Assert.assertEquals("Invalid number of nodes in graph", 8, graph.size());
		Assert.assertEquals("Invalid number of start nodes in graph", 2, graph.getHeads().size());
		Assert.assertEquals("Invalid number of exit nodes in graph", 2, graph.getTails().size());
		
		// Find some nodes
		Node n1 = null, n2 = null, n3 = null;
		List<Node> workList = new ArrayList<Node>();
		List<Node> doneList = new ArrayList<Node>();
		workList.addAll(graph.getHeads());
		while (!workList.isEmpty()) {
			Node n = workList.remove(0);
			if (doneList.contains(n))
				continue;
			doneList.add(n);
			
			if (n.toString().equals("<n1>"))
				n1 = n;
			else if (n.toString().equals("<n2>"))
				n2 = n;
			else if (n.toString().equals("<n3>"))
				n3 = n;
			
			workList.addAll(graph.getSuccsOf(n));
		}
		Assert.assertNotNull("Node n1 not found", n1);
		Assert.assertNotNull("Node n2 not found", n2);
		Assert.assertNotNull("Node n3 not found", n3);
		
//		Assert.assertTrue("Edge n1->n2 not found", graph.containsEdge(n1, n2));
//		Assert.assertTrue("Edge n2->n3 not found", graph.containsEdge(n2, n3));
		
	}

}
