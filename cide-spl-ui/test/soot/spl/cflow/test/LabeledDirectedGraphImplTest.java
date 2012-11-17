package soot.spl.cflow.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the {@link LabeledDirectedGraphImpl} class.
 *
 * @author Steven Arzt
 *
 */
public class LabeledDirectedGraphImplTest {

	private LabeledDirectedGraphImpl<SimpleNode, String> graph = new LabeledDirectedGraphImpl<SimpleNode, String>();
	private SimpleNode n1 = new SimpleNode("n1", true, false);
	private SimpleNode n2 = new SimpleNode("n2");
	private SimpleNode n3 = new SimpleNode("n3", false, true);
	private SimpleNode n4 = new SimpleNode("n4");
	private SimpleNode n5 = new SimpleNode("n5", false, true);
	private SimpleNode n6 = new SimpleNode("n6", true, false);
	private SimpleNode n7 = new SimpleNode("n7");
	private SimpleNode n8 = new SimpleNode("n8", false, true);

	@Before
	public void buildSimpleGraph() {
		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
		graph.addNode(n4);
		graph.addNode(n5);
		graph.addNode(n6);
		graph.addNode(n7);
		graph.addNode(n8);
		Assert.assertEquals("Invalid number of nodes in graph", 8, graph.getNodes().size());
		
		graph.addEdge(n1, n2);
		graph.addEdge(n2, n3);
		graph.addEdge(n2, n4);
		graph.addEdge(n3, n4);
		graph.addEdge(n4, n5);

		graph.addEdge(n6, n7);
		graph.addEdge(n7, n6);
		graph.addEdge(n7, n8);
	}

	/**
	 * Tests the constructors and the "getNodes" method
	 */
	@Test
	public void testConstruct() {
		LabeledDirectedGraphImpl<SimpleNode, String> graph = new LabeledDirectedGraphImpl<SimpleNode, String>();
		Assert.assertTrue("Initial graph contained nodes", graph.getNodes().isEmpty());
		Assert.assertEquals("Initial graph contained wrong number of nodes", 0, graph.size());

		SimpleNode n = new SimpleNode("foo");
		LabeledDirectedGraphImpl<SimpleNode, String> graph2 = new LabeledDirectedGraphImpl<SimpleNode, String>(n);
		Assert.assertEquals("Initial graph contained wrong number of nodes", 1, graph2.getNodes().size());
		Assert.assertEquals("Initial graph contained wrong number of nodes", 1, graph2.size());
		Assert.assertTrue("Node not found", graph2.getNodes().contains(n));
		
		SimpleNode n2 = new SimpleNode("bar");
		List<SimpleNode> nodes = new ArrayList<SimpleNode>();
		nodes.add(n);
		nodes.add(n2);
		LabeledDirectedGraphImpl<SimpleNode, String> graph3 = new LabeledDirectedGraphImpl<SimpleNode, String>(nodes);
		Assert.assertEquals("Initial graph contained wrong number of nodes", 2, graph3.getNodes().size());
		Assert.assertEquals("Initial graph contained wrong number of nodes", 2, graph3.size());
		Assert.assertTrue("Node not found", graph3.getNodes().contains(n));
		Assert.assertTrue("Node not found", graph3.getNodes().contains(n2));
	}
	
	@Test
	public void testAddNode() {
		SimpleNode n = new SimpleNode("foo");
		LabeledDirectedGraphImpl<SimpleNode, String> graph = new LabeledDirectedGraphImpl<SimpleNode, String>(n);
		graph.addNode(n);
		Assert.assertEquals("Wrong number of nodes in graph", 1, graph.getNodes().size());

		SimpleNode n2 = new SimpleNode("foo");
		graph.addNode(n2);
		Assert.assertEquals("Wrong number of nodes in graph", 2, graph.getNodes().size());
	}
		
	@Test
	public void testHeads() {
		Assert.assertEquals("Invalid number of heads found", 2, graph.getHeads().size());
		boolean found1 = false;
		boolean found6 = false;
		for (SimpleNode n : graph.getHeads())
			if (n.getNodeName().equals("n1"))
				found1 = true;
			else if (n.getNodeName().equals("n6"))
				found6 = true;
		Assert.assertTrue("Expected head node n1 not found", found1);
		Assert.assertTrue("Expected head node n6 not found", found6);
	}

	@Test
	public void testTails() {
		Assert.assertEquals("Invalid number of tails found", 3, graph.getTails().size());
		boolean found3 = false;
		boolean found5 = false;
		boolean found8 = false;
		for (SimpleNode n : graph.getTails())
			if (n.getNodeName().equals("n3"))
				found3 = true;
			else if (n.getNodeName().equals("n5"))
				found5 = true;
			else if (n.getNodeName().equals("n8"))
				found8 = true;
		Assert.assertTrue("Expected head node n1 not found", found3);
		Assert.assertTrue("Expected head node n6 not found", found5);
		Assert.assertTrue("Expected head node n6 not found", found8);
	}

	@Test
	public void testGetPredsOf() {
		Assert.assertEquals("Invalid predecessors for node n1", 0, graph.getPredsOf(n1).size());
		Assert.assertEquals("Invalid predecessors for node n2", 1, graph.getPredsOf(n2).size());
		Assert.assertEquals("Invalid predecessors for node n3", 1, graph.getPredsOf(n3).size());
		Assert.assertEquals("Invalid predecessors for node n4", 2, graph.getPredsOf(n4).size());
		Assert.assertEquals("Invalid predecessors for node n5", 1, graph.getPredsOf(n5).size());

		Assert.assertEquals("Invalid predecessors for node n6", 1, graph.getPredsOf(n6).size());
		Assert.assertEquals("Invalid predecessors for node n7", 1, graph.getPredsOf(n7).size());
		Assert.assertEquals("Invalid predecessors for node n8", 1, graph.getPredsOf(n8).size());
	}

	@Test
	public void testGetSuccsOf() {
		Assert.assertEquals("Invalid successors for node n1", 1, graph.getSuccsOf(n1).size());
		Assert.assertEquals("Invalid successors for node n2", 2, graph.getSuccsOf(n2).size());
		Assert.assertEquals("Invalid successors for node n3", 1, graph.getSuccsOf(n3).size());
		Assert.assertEquals("Invalid successors for node n4", 1, graph.getSuccsOf(n4).size());
		Assert.assertEquals("Invalid successors for node n5", 0, graph.getSuccsOf(n5).size());

		Assert.assertEquals("Invalid successors for node n6", 1, graph.getSuccsOf(n6).size());
		Assert.assertEquals("Invalid successors for node n7", 2, graph.getSuccsOf(n7).size());
		Assert.assertEquals("Invalid successors for node n8", 0, graph.getSuccsOf(n8).size());
	}
	
	@Test
	public void testContainsEdge() {
		Assert.assertTrue("Edge not found", graph.containsEdge(n1, n2));
		Assert.assertTrue("Edge not found", graph.containsEdge(n2, n3));
		Assert.assertTrue("Edge not found", graph.containsEdge(n2, n4));
		Assert.assertTrue("Edge not found", graph.containsEdge(n3, n4));
		Assert.assertTrue("Edge not found", graph.containsEdge(n4, n5));

		Assert.assertTrue("Edge not found", graph.containsEdge(n6, n7));
		Assert.assertTrue("Edge not found", graph.containsEdge(n7, n6));
		Assert.assertTrue("Edge not found", graph.containsEdge(n7, n8));

		Assert.assertFalse("Invalid edge found", graph.containsEdge(n1, n4));
		Assert.assertFalse("Invalid edge found", graph.containsEdge(n8, n4));
		Assert.assertFalse("Invalid edge found", graph.containsEdge(n8, n6));
	}
	
	@Test
	public void testGetEdge() throws NoSuchEdgeException {
		Assert.assertNotNull("Edge not found", graph.getEdge(n1, n2));
		Assert.assertNotNull("Edge not found", graph.getEdge(n2, n3));
		Assert.assertNotNull("Edge not found", graph.getEdge(n2, n4));
		Assert.assertNotNull("Edge not found", graph.getEdge(n3, n4));
		Assert.assertNotNull("Edge not found", graph.getEdge(n4, n5));

		Assert.assertNotNull("Edge not found", graph.getEdge(n6, n7));
		Assert.assertNotNull("Edge not found", graph.getEdge(n7, n6));
		Assert.assertNotNull("Edge not found", graph.getEdge(n7, n8));

		boolean ok = false;
		try {
			graph.getEdge(n1, n4);
		}
		catch (NoSuchEdgeException ex) {
			ok = true;
		}
		Assert.assertTrue("Invalid edge found", ok);
	}
	
	@Test
	public void testRemoveEdge() {
		graph.removeEdge(n1, n2);
		Assert.assertFalse("Invalid edge found", graph.containsEdge(n1, n2));
	}

	@Test
	public void testRemoveNode() {
		graph.removeNode(n1);
		Assert.assertFalse("Invalid edge found", graph.containsEdge(n1, n2));
		Assert.assertFalse("Invalid node found", graph.containsNode(n1));
	}
	
	@Test
	public void testContainsNode() {
		Assert.assertTrue("Node not found", graph.containsNode(n1));
		Assert.assertTrue("Node not found", graph.containsNode(n2));
		Assert.assertTrue("Node not found", graph.containsNode(n3));
		Assert.assertTrue("Node not found", graph.containsNode(n4));
		Assert.assertTrue("Node not found", graph.containsNode(n5));
		Assert.assertTrue("Node not found", graph.containsNode(n6));
		Assert.assertTrue("Node not found", graph.containsNode(n7));
		Assert.assertTrue("Node not found", graph.containsNode(n8));

		Assert.assertFalse("Invalid node found", graph.containsNode(new SimpleNode("whatever")));
	}

}
