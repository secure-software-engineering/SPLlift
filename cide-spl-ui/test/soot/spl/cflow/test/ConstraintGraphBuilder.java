package soot.spl.cflow.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import net.sf.javabdd.BDDFactory;
import soot.spl.cflow.LabeledDirectedGraph;
import soot.spl.ifds.Constraint;
import soot.util.NumberedString;
import soot.util.StringNumberer;

public class ConstraintGraphBuilder {
	
	private StringNumberer numberer;

	public LabeledDirectedGraph<Node, Constraint<String>> buildGraphFromFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		
		Constraint.FACTORY = BDDFactory.init(100000, 100000);
		Constraint.FACTORY.setVarNum(100); //some number high enough to accommodate the max. number of features; ideally we should compute this number 
		
		LabeledDirectedGraphImpl<Node, Constraint<String>> graph = new AugmentedLabeledDirectedGraphImpl<Node, Constraint<String>>();
		Map<String, SimpleNode> nodeCache = new HashMap<String, SimpleNode>();
		
		numberer = new StringNumberer();
		
		int lineNum = 1;
		String line;
		while((line=reader.readLine())!=null) {
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
				System.err.println("Syntax error on line " + lineNum + ", resulting graph may be incomplete. "
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
			
			Constraint<String> constraint;
			if(!label.isEmpty()) {
				boolean negate = false;
				if(label.startsWith("not")) {
					label = label.substring(3);
					negate = true;
				}
				NumberedString numberedLabel = numberer.findOrAdd(label);
				BitSet bs = new BitSet();
				bs.set(numberedLabel.getNumber());
				constraint = Constraint.make(bs,!negate);
			} else {
				constraint = Constraint.trueValue();
			}
			
			Edge<Node, Constraint<String>> edge = new Edge<Node, Constraint<String>>(node1, node2, constraint);
			graph.addEdge(edge);
			
			lineNum++;
		}
		
		return graph;
	}
	
	public StringNumberer getLabelNumberer() {
		return numberer;
	}

}
