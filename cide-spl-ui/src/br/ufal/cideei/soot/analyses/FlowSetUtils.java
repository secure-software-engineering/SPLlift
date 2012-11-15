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

package br.ufal.cideei.soot.analyses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import soot.Body;
import soot.PatchingChain;
import soot.Unit;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import br.ufal.cideei.soot.instrument.ConfigTag;
import br.ufal.cideei.soot.instrument.IConfigRep;
import br.ufal.cideei.soot.instrument.ILazyConfigRep;

/**
 * Utility class for collecting data/metrics about FlowSets.
 * 
 * @author Társis
 * 
 */
public class FlowSetUtils {

	/**
	 * Count the number of units necessary to store the results of analysing body with a simultaneous analysis.
	 * 
	 * @param body
	 *            the analyzed body
	 * @param analysis
	 *            the analysis used on body
	 * @param countKeys
	 *            if keys in the Map of the MapLiftedFlowSet should be taken into consideration
	 * @param countEmptyAs
	 *            how much should empty FlowSets should count? e.g. 0 or 1?
	 * @return
	 */
	public static int liftedMemoryUnits(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, boolean countKeys, int countEmptyAs) {
		int memUnits = 0;
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			MapLiftedFlowSet flowAfter = analysis.getFlowAfter(unit);
			MapLiftedFlowSet flowBefore = analysis.getFlowBefore(unit);

			Set<Entry<IConfigRep, FlowSet>> entrySet;

			entrySet = flowBefore.getMapping().entrySet();
			for (Entry<IConfigRep, FlowSet> entry : entrySet) {
				FlowSet value = entry.getValue();
				if (value.isEmpty()) {
					memUnits += countEmptyAs;
				} else {
					memUnits += value.size();
				}
				if (countKeys) {
					memUnits++;
				}
			}

			entrySet = flowAfter.getMapping().entrySet();
			for (Entry<IConfigRep, FlowSet> entry : entrySet) {
				FlowSet value = entry.getValue();
				if (value.isEmpty()) {
					memUnits += countEmptyAs;
				} else {
					memUnits += value.size();
				}
				if (countKeys) {
					memUnits++;
				}
			}
		}
		return memUnits;
	}

	/**
	 * Count the number of units necessary to store the results of analysing body with a consecutive analysis.
	 * 
	 * @param body the analyzed body
	 * @param analysis the analysis that ran on body
	 * @param countEmptyAs how much should empty FlowSet count? 0? 1?...
	 * @return
	 */
	public static long unliftedMemoryUnits(Body body, ForwardFlowAnalysis<Unit, FlowSet> analysis, int countEmptyAs) {
		long memUnits = 0;
		PatchingChain<Unit> units = body.getUnits();
		for (Unit unit : units) {
			int flowBeforeSize = analysis.getFlowBefore(unit).size();
			if (flowBeforeSize == 0) {
				memUnits += countEmptyAs;
			} else {
				memUnits += flowBeforeSize;
			}

			int flowAfterSize = analysis.getFlowAfter(unit).size();
			if (flowAfterSize == 0) {
				memUnits += countEmptyAs;
			} else {
				memUnits += flowAfterSize;
			}
		}
		return memUnits;
	}
	
	//#ifdef LAZY
//@	/**
//@	 * Calculates the average sharing degree of lazy flow sets.
//@	 * @param body
//@	 * @param analysis
//@	 * @return
//@	 */
//@	public static double averageSharingDegree(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis) {
//@		ConfigTag tag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
//@		ILazyConfigRep lazyConfig = (ILazyConfigRep) tag.getConfigReps().iterator().next();
//@
//@		double noOfConfigs = lazyConfig.size();
//@
//@		List<Double> sharingDegrees = new ArrayList<Double>();
//@		PatchingChain<Unit> units = body.getUnits();
//@		for (Unit unit : units) {
//@			MapLiftedFlowSet flowBefore = analysis.getFlowBefore(unit);
//@			MapLiftedFlowSet flowAfter = analysis.getFlowAfter(unit);
//@
//@			sharingDegrees.add(noOfConfigs / flowBefore.size());
//@			sharingDegrees.add(noOfConfigs / flowAfter.size());
//@		}
//@
//@		double accumulator = 0.0;
//@		for (Double degree : sharingDegrees) {
//@			accumulator += degree;
//@		}
//@
//@		return accumulator / sharingDegrees.size();
//@	}
//@
//@	/**
//@	 * Creates a PBM pixel matrix of an lazy analysis.
//@	 * 
//@	 * @param body
//@	 * @param analysis
//@	 * @param fileName
//@	 * @return
//@	 */
//@	public static File pbm(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, String fileName) {
//@		ConfigTag tag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
//@		final int MAX_WIDTH = tag.getConfigReps().iterator().next().size();
//@		List<LinkedList<Integer>> matrix = createPixMatrix(body, analysis, MAX_WIDTH);
//@
//@		createPixMatrix(body, analysis, MAX_WIDTH);
//@
//@		OutputStream streamOut;
//@		try {
//@			streamOut = new FileOutputStream(fileName);
//@		} catch (FileNotFoundException e) {
//@			e.printStackTrace();
//@			return null;
//@		}
//@		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
//@		writerOut.println("P2");
//@		writerOut.println(matrix.get(0).size() + " " + matrix.size());
//@		writerOut.println("10");
//@		for (LinkedList<Integer> row : matrix) {
//@			for (Integer pix : row) {
//@				writerOut.print(pix);
//@				writerOut.print(' ');
//@			}
//@			writerOut.println("");
//@		}
//@		writerOut.flush();
//@		try {
//@			streamOut.close();
//@		} catch (IOException e) {
//@			// TODO Auto-generated catch block
//@			e.printStackTrace();
//@			return null;
//@		}
//@		return new File(fileName);
//@	}
//@
//@	private static List<LinkedList<Integer>> createPixMatrix(Body body, ForwardFlowAnalysis<Unit, MapLiftedFlowSet> analysis, final int MAX_WIDTH) {
//@		List<LinkedList<Integer>> matrix = new ArrayList<LinkedList<Integer>>();
//@		int index = 0, size = 0;
//@		PatchingChain<Unit> units = body.getUnits();
//@		for (Unit unit : units) {
//@			size = analysis.getFlowAfter(unit).size();
//@
//@			LinkedList<Integer> row = new LinkedList<Integer>();
//@			for (index = 0; index < size; index++) {
//@				row.add(0);
//@			}
//@			boolean headTailFlip = true;
//@			while (row.size() != MAX_WIDTH) {
//@				if (headTailFlip)
//@					row.addFirst(9);
//@				else
//@					row.addLast(9);
//@
//@				headTailFlip = !headTailFlip;
//@			}
//@			matrix.add(row);
//@		}
//@		return matrix;
//@	}
//@	
	//#endif
}
