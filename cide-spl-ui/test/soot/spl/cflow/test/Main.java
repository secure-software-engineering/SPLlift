package soot.spl.cflow.test;

import java.io.File;
import java.io.IOException;

import soot.spl.cflow.ConditionalPostdominators;
import soot.spl.cflow.LabeledDirectedGraph;
import soot.spl.ifds.Constraint;

public class Main {

	public static void main(String[] args) throws IOException {
		if(args.length<1) {
			System.err.println("Must give file argument");
			System.exit(1);
		}
		
		String fileName = args[0];
		
		File inputFile = new File(fileName);
		
		ConstraintGraphBuilder builder = new ConstraintGraphBuilder();
		
		LabeledDirectedGraph<Node, Constraint<String>> graph = builder.buildGraphFromFile(inputFile);
		
		System.err.println(graph);
		
		ConditionalPostdominators<String, Node> cpd = new ConditionalPostdominators<String, Node>(graph, builder.getLabelNumberer());
		
		cpd.print();
		
//		ConditionalProgramDependenceGraph<String, Node> cpdg = new ConditionalProgramDependenceGraph<String,Node>(graph);
//		
		
		
	}

}
