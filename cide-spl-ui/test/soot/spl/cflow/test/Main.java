package soot.spl.cflow.test;

import java.io.File;
import java.io.IOException;

import soot.spl.cflow.ConditionalControlDependenceGraph;
import soot.spl.cflow.ConditionalImmediatePostdominators;
import soot.spl.cflow.ConditionalPostdominators;
import soot.spl.cflow.FastConditionalControlDependenceGraph;
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
				
		ConditionalPostdominators<String, Node> cpd = new ConditionalPostdominators<String, Node>(graph, builder.getLabelNumberer());
		
		cpd.print();
		cpd.outputGraphViz(inputFile.getName());

		ConditionalImmediatePostdominators<String, Node> cipd = new ConditionalImmediatePostdominators<String, Node>(graph, builder.getLabelNumberer());
		
		cipd.print();
		cipd.outputGraphViz(inputFile.getName());
		
		ConditionalControlDependenceGraph<String, Node> cpdg = new ConditionalControlDependenceGraph<String,Node>(graph, builder.getLabelNumberer());
		
		cpdg.print();
		cpdg.outputGraphViz(inputFile.getName());

		FastConditionalControlDependenceGraph<String, Node> fcpdg = new FastConditionalControlDependenceGraph<String,Node>(graph, builder.getLabelNumberer());
		
		fcpdg.print();
		fcpdg.outputGraphViz(inputFile.getName());

	}

}
