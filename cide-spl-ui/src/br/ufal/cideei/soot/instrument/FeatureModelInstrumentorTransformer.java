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

package br.ufal.cideei.soot.instrument;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.CompilationUnit;

import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;
import soot.Unit;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnPosTag;
import soot.util.NumberedString;
import soot.util.StringNumberer;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.util.CachedICompilationUnitParser;

/**
 * The Class FeatureModelInstrumentor is a Soot transformation for transcribing feature information to every Unit. This
 * is done via Tag annonations (FeatureTag).
 */
// TODO: change class name to something more appropriate, like
// "FeatureInstrumentorTransformer"
public class FeatureModelInstrumentorTransformer extends BodyTransformer {

	/** Feature extracter. */
	private static IFeatureExtracter extracter;
	/** Current compilation unit the transformation is working on */
	private IFile file;

	/**
	 * XXX: Workaround for the preTransform method. See comments on FeatureModelInstrumentorTransformer#preTransform()
	 * method.
	 */
	private static String classPath;
	private CachedICompilationUnitParser cachedParser = new CachedICompilationUnitParser();
	private CachedLineNumberMapper cachedLineColorMapper = new CachedLineNumberMapper();
	private Map<Integer, Set<String>> currentColorMap;
	
	private StringNumberer featureNumberer = new StringNumberer();

	// #ifdef METRICS
//@	protected static long transformationTime = 0;
//@	protected static long parsingTime = 0;
//@	protected static long colorLookupTableBuildingTime = 0;
//@	protected AbstractMetricsSink sink;
//@	protected static String COLOR_LOOKUP = "color table";
//@	protected static String PARSING = "parsing";
//@	protected static String INSTRUMENTATION = "instrumentation";
//@
	// #endif

	/*
	 * TODO: maybe injecting the sink depency in a different way could make this funcionality less intrusive.
	 */
	public FeatureModelInstrumentorTransformer(IFeatureExtracter extracter, String classPath) {
		FeatureModelInstrumentorTransformer.classPath = classPath;
		FeatureModelInstrumentorTransformer.extracter = extracter;
	}

	// #ifdef METRICS
//@	public FeatureModelInstrumentorTransformer setMetricsSink(AbstractMetricsSink sink) {
//@		this.sink = sink;
//@		return this;
//@	}
//@
	// #endif

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String phase, @SuppressWarnings("rawtypes") Map options) {
		SootClass sootClass = body.getMethod().getDeclaringClass();
		if(!sootClass.isApplicationClass()) return; //if no application class, then we don't care

		if (!sootClass.hasTag("SourceFileTag")) {
			throw new IllegalArgumentException("the body cannot be traced to its source file");
		}		
		
		preTransform(body);

		// #ifdef METRICS
//@		long startTransform = System.nanoTime();
		// #endif

		/*
		 * Iterate over all units, look up for their colors and add a new FeatureTag to each of them, and also compute
		 * all the colors found in the whole body. Units with no colors receive an empty FeatureTag.
		 */
		Iterator<Unit> unitIt = body.getUnits().iterator();

		/*
		 * After the following loop, allPresentFeatures will hold all the colors found in the body. Used to calculate a
		 * "local" power set.
		 */
		Set<String> allPresentFeatures = new HashSet<String>();

		/*
		 * The set of features are represented as bits, for a more compact representation. The mapping between a feature
		 * and it's ID is stored in the FeatureTag of the body.
		 * 
		 * This is necessary so that clients, such as r.d. analysis, can safely iterate over all configurations without
		 * explicitly invoking Set operations like containsAll();
		 * 
		 * TODO: check redundancy between allPresentFeatures & allPresentFeaturesId
		 */

		// String->Integer
//		BidiMap allPresentFeaturesId = new DualHashBidiMap();
		//FeatureTag emptyFeatureTag;
		// #ifdef LAZY
//@		BitVectorFeatureRep emptyBitVectorRep = new BitVectorFeatureRep(Collections.EMPTY_SET, allPresentFeaturesId);
//@		emptyFeatureTag = new FeatureTag(emptyBitVectorRep);
//@		
		// #else
		//emptyFeatureTag = new FeatureTag(new BitFeatureRep(Collections.EMPTY_SET, allPresentFeaturesId));

		// #endif

		// #ifdef LAZY
//@		/*
//@		 * in the lazy approach, the representation can only be consolidade after all features have been discovery. All
//@		 * IFeatureRep will stored so that it is possible to consolidade later.
//@		 */
//@		List<BitVectorFeatureRep> generateVectorLater = new ArrayList<BitVectorFeatureRep>();
		// #endif

		BitSet union = new BitSet();
		BitSet intersection = new BitSet(1000); // 1000 = some sufficiently large number
		intersection.set(0,1000);
		while (unitIt.hasNext()) {
			Unit nextUnit = unitIt.next();
			SourceLnPosTag lineTag = (SourceLnPosTag) nextUnit.getTag("SourceLnPosTag");
			if (lineTag == null) {
				//nextUnit.addTag(emptyFeatureTag);
			} else {
				int unitLine = lineTag.startLn();
				Set<String> nextUnitColors = currentColorMap.get(unitLine);
				if (nextUnitColors != null) {

//					for (String color : nextUnitColors) {
//						if (!allPresentFeaturesId.containsKey(color)) {
//							allPresentFeaturesId.put(color, idGen);
//							idGen = idGen << 1;
//						}
//					}
					/*
					 * increment local powerset with new found colors.
					 */
//					allPresentFeatures.addAll(nextUnitColors);

					IFeatureRep featRep;
					FeatureTag featureTag;
					// #ifdef LAZY
//@					featRep = new BitVectorFeatureRep(nextUnitColors, allPresentFeaturesId);
//@					generateVectorLater.add((BitVectorFeatureRep) featRep);
//@					featureTag = new FeatureTag(featRep);
//@					nextUnit.addTag(featureTag);
					// #else
					
//					 featRep = new BitFeatureRep(nextUnitColors, allPresentFeaturesId);
					
					BitSet bs = new BitSet();
					for (String color : nextUnitColors) {
						NumberedString numberedString = featureNumberer.findOrAdd(color);
						bs.set(numberedString.getNumber());
					}

					
					// #endif
					featureTag = new FeatureTag(bs);
					nextUnit.addTag(featureTag);
					
					union.or(bs);
					intersection.and(bs);
				} else {
					intersection.clear();
					//nextUnit.addTag(emptyFeatureTag);
				}
			}
		}
		intersection.and(union);
		if(!intersection.isEmpty()) {
			FeatureTag t = new FeatureTag(intersection);
			body.addTag(t);
			body.getMethod().addTag(t);
		}
//		UnmodifiableBidiMap unmodAllPresentFeaturesId = (UnmodifiableBidiMap) UnmodifiableBidiMap.decorate(allPresentFeaturesId);

		// #ifdef LAZY
//@		/*
//@		 * generate vectors
//@		 */
//@
//@		for (BitVectorFeatureRep featureRep : generateVectorLater) {
//@			featureRep.generateBitVector(idGen);
//@		}
		// #endif

		// #ifdef METRICS
//@		long transformationDelta = System.nanoTime() - startTransform;
//@		if (sink != null) {
//@			sink.flow(body, FeatureModelInstrumentorTransformer.INSTRUMENTATION, transformationDelta);
//@		}
//@		FeatureModelInstrumentorTransformer.transformationTime += transformationDelta;
		// #endif

		ConfigTag configTag;

		// #ifdef LAZY
//@
//@		BitVectorConfigRep localConfigurations = BitVectorConfigRep.localConfigurations(idGen, unmodAllPresentFeaturesId);
//@		emptyBitVectorRep.generateBitVector(idGen);
//@		
//@		Set<IConfigRep> lazyConfig = new HashSet<IConfigRep>();
//@		lazyConfig.add(localConfigurations);
//@		configTag = new ConfigTag(lazyConfig);
//@		body.addTag(configTag);
//@
		// #else
		
//		 configTag = new ConfigTag(BitConfigRep.localConfigurations(idGen, unmodAllPresentFeaturesId).getConfigs());
//		 body.addTag(configTag);
//		
		// #endif
	}

	/**
	 * Do the transformation on the body. To accomplish this, the class that declares this SootMethod needs to be tagged
	 * with the SourceFileTag.
	 * 
	 * @param body
	 *            the body
	 * @param compilationUnit
	 *            the compilation unit
	 */
	public void transform2(Body body, String classPath) {
		FeatureModelInstrumentorTransformer.classPath = classPath;
		preTransform(body);
		this.transform(body);
	}

	private void preTransform(Body body) {
		SootClass sootClass = body.getMethod().getDeclaringClass();
		/*
		 * XXX: WARNING! tag.getAbsolutePath() returns an INCORRECT value for the absolute path AFTER the first body
		 * transformation. In this workaround, since this method depends on the classpath , it is injected on this class
		 * constructor. We will use tag.getSourceFile() in order to resolve the file name.
		 * 
		 * Yes, this is ugly.
		 */
		SourceFileTag sourceFileTag = (SourceFileTag) sootClass.getTag("SourceFileTag");

//		/*
//		 * The String absolutePath will be transformed to the absolute path to the Class which body belongs to. See the
//		 * XXX above for the explanation.
//		 */
//		String absolutePath = sootClass.getName();
//		int lastIndexOf = absolutePath.lastIndexOf(".");
//		if (lastIndexOf != -1) {
//			absolutePath = absolutePath.substring(0, lastIndexOf);
//		} else {
//			absolutePath = "";
//		}
//
//		/*
//		 * XXX String#replaceAll does not work properly when replacing "special" chars like File.separator. The Matcher
//		 * and Pattern composes a workaround for that.
//		 */
//		absolutePath = absolutePath.replaceAll(Pattern.quote("."), Matcher.quoteReplacement(File.separator));
//		absolutePath = classPath + File.separator + absolutePath + File.separator + sourceFileTag.getSourceFile();
//		sourceFileTag.setAbsolutePath(absolutePath);
		
		String abspath = sourceFileTag.getAbsolutePath();
		if(abspath==null) return;

		IPath path = new Path(abspath);
		this.file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		// #ifdef METRICS
//@		long startCompilationUnitParser = System.nanoTime();
		// #endif
		CompilationUnit compilationUnit = cachedParser.parse(file);
		// #ifdef METRICS
//@		long parsingDelta = System.nanoTime() - startCompilationUnitParser;
//@		if (sink != null)
//@			sink.flow(body, FeatureModelInstrumentorTransformer.PARSING, parsingDelta);
//@		FeatureModelInstrumentorTransformer.parsingTime += parsingDelta;
//@
//@		long startBuilderColorLookUpTable = System.nanoTime();
		// #endif
		this.currentColorMap = cachedLineColorMapper.makeAccept(compilationUnit, file, extracter, compilationUnit);
		// #ifdef METRICS
//@		long builderColorLookUpTableDelta = System.nanoTime() - startBuilderColorLookUpTable;
//@		if (sink != null)
//@			sink.flow(body, FeatureModelInstrumentorTransformer.COLOR_LOOKUP, builderColorLookUpTableDelta);
//@		FeatureModelInstrumentorTransformer.colorLookupTableBuildingTime += builderColorLookUpTableDelta;
		// #endif
	}
	
	public int numFeaturesPresent() {
		return featureNumberer.size();
	}
	
	public StringNumberer getFeatureNumberer() {
		return featureNumberer;
	}
}
