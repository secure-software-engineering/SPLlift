/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.soot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Printer;
import soot.SourceLocator;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;

/**
 * The Class UnitUtil contains convenience methods to serialize information from memory to help with visualization and
 * debugging.
 */
public class UnitUtil {

	/**
	 * This is a utility class. There's no need for a constructor since all methods are static.
	 */
	private UnitUtil() {
	}

	/**
	 * Serialize a SootMethod Body in the Jimple IR into a provided file path.
	 * 
	 * TODO: describe the behaviour of this method in case the file already exists.
	 * 
	 * @param body
	 *            the body
	 * @param fileName
	 *            the file name, or null to use the default Soot output folder.
	 * @return
	 */
	public static File serializeBody(Body body, String fileName) {
		/*
		 * prints .jimple file
		 */
		if (fileName == null) {
			fileName = SourceLocator.v().getFileNameFor(body.getMethod().getDeclaringClass(), Options.output_format_jimple);
		}

		OutputStream streamOut;
		try {
			streamOut = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
		Printer.v().printTo(body, writerOut);
		writerOut.flush();
		try {
			streamOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new File(fileName);
	}

	/**
	 * Serialize a Unit graph in the DOT format into a provided file path.
	 * 
	 * TODO: describe the behaviour of this method in case the file already exists.
	 * 
	 * @param body
	 *            the body
	 * @param fileName
	 *            the file name, or null to use the default Soot output folder.
	 */
	public static File serializeGraph(Body body, String fileName) {
		CFGGraphType graphType = CFGGraphType.BRIEF_UNIT_GRAPH;
		DirectedGraph graph = graphType.buildGraph(body);
		SootUnitGraphSerializer drawer = new SootUnitGraphSerializer();
		DotGraph canvas = graphType.drawGraph(drawer, graph, body);
		String methodName = body.getMethod().getSubSignature();

		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + File.separator;
			}
			fileName = fileName + methodName.replace(File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
		}

		canvas.plot(fileName);
		return new File(fileName);
	}

	/**
	 * Serialize a CallGraph in the DOT format into a provided file path.
	 * 
	 * TODO: describe the behaviour of this method in case the file already exists.
	 * 
	 * @param graph
	 *            the graph to be serialized
	 * @param fileName
	 *            the file name, or null to use the default Soot output folder.
	 */
	public static File serializeCallGraph(CallGraph graph, String fileName) {
		if (fileName == null) {
			fileName = soot.SourceLocator.v().getOutputDir();
			if (fileName.length() > 0) {
				fileName = fileName + java.io.File.separator;
			}
			fileName = fileName + "call-graph" + DotGraph.DOT_EXTENSION;
		}
		DotGraph canvas = new DotGraph("call-graph");
		QueueReader<Edge> listener = graph.listener();
		while (listener.hasNext()) {
			Edge next = listener.next();
			MethodOrMethodContext src = next.getSrc();
			MethodOrMethodContext tgt = next.getTgt();
			canvas.drawNode(src.toString());
			canvas.drawNode(tgt.toString());
			canvas.drawEdge(src.toString(), tgt.toString());
		}
		canvas.plot(fileName);
		return new File(fileName);
	}
}
