package soot.spl.ifds;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.javabdd.BDDFactory;

import org.eclipse.jdt.core.IJavaProject;

import soot.Body;
import soot.BodyTransformer;
import soot.Pack;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import soot.spl.cflow.FastConditionalControlDependenceGraph;
import soot.spl.cflow.LabeledUnitGraph;
import soot.spl.cflow.baseline.TraditionalControlDependenceGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.NumberedString;
import soot.util.StringNumberer;
import br.ufal.cideei.features.CIDEFeatureExtracterFactory;
import br.ufal.cideei.features.IFeatureExtracter;
import br.ufal.cideei.handlers.AnalysisArgs;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;

public class Main {
	
	public static long beforeSootStart;
	
	public static void main(final String classpath, String mainClass, final IJavaProject javaProject, final AnalysisArgs arg) {
		try {
			String j2me = arg.j2me ? "-j2me -x org. -x com. -x java -ire -allow-phantom-refs " : "";
			
			String string = "-cp "+classpath+" -f none -src-prec java -app "+j2me+"-main-class "+mainClass+" "+mainClass;
			String[] args = string.split(" ");
			
			Pack pack = PackManager.v().getPack("jtp");
			if(pack.get("jtp.ifds")==null) {
				pack.add(new Transform("jtp.ifds", new BodyTransformer() {

					protected void internalTransform(Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
						IFeatureExtracter extracter = CIDEFeatureExtracterFactory.getInstance().getExtracter();
						FeatureModelInstrumentorTransformer bodyTransformer = new FeatureModelInstrumentorTransformer(extracter,classpath);
						bodyTransformer.transform(b);

						Constraint.FACTORY = BDDFactory.init(100000, 100000);
						Constraint.FACTORY.setVarNum(100); //some number high enough to accommodate the max. number of features; ideally we should compute this number
						
						final int NUM=100;

						LabeledUnitGraph graph = new LabeledUnitGraph(new ExceptionalUnitGraph(b));
						long beforeSPL = System.nanoTime();
						for(int i=0;i<NUM;i++)
							new FastConditionalControlDependenceGraph<String, Unit>(graph);
						long afterSPL = System.nanoTime();
						System.err.println(afterSPL-beforeSPL);
						
						
						StringNumberer featureNumberer = bodyTransformer.getFeatureNumberer();
						Set<NumberedString> featuresReachableInCode = new HashSet<NumberedString>();
						for (NumberedString ns : featureNumberer) {
							featuresReachableInCode.add(ns);
						}
						final Iterable<Set<NumberedString>> powerSet = Powerset.powerset(featuresReachableInCode);
						long acc = 0;
						for (Set<NumberedString> config: powerSet) {
							BitSet bs = new BitSet();
							for (NumberedString feature: config) {
								bs.set(feature.getNumber());
							}							
							Constraint<String> configConstraint = Constraint.make(bs, featuresReachableInCode);
							long beforeTraditional = System.nanoTime();
							for(int i=0;i<NUM;i++)
								new TraditionalControlDependenceGraph<String,Unit>(graph, configConstraint);
							long afterTraditional = System.nanoTime();
							acc += afterTraditional - beforeTraditional;
						}						
						System.err.println(acc);

					
//						final Multimap<SootMethod,Local> initialSeeds = HashMultimap.create();
//						initialSeeds.put(Scene.v().getMainMethod(), Scene.v().getMainMethod().getActiveBody().getLocals().getFirst());
//												
////						IFDSTabulationProblem<Unit,Value,SootMethod> problem = new heros.problems.IFDSUninitializedVariables();
////						
////						
////						SPLIFDSSolver<Value,SootMethod> solver = new SPLIFDSSolver<Value,SootMethod>(problem,alloyFilePath,numFeaturesPresent);	
////						long before = System.currentTimeMillis();
////						System.err.println("Starting solver...");
////						solver.solve();
////						System.err.println("Solving took: "+(System.currentTimeMillis()-before));
////						System.err.println(Scene.v().getMainMethod().getActiveBody());
////						Unit ret = Scene.v().getMainMethod().getActiveBody().getUnits().getLast();
////						for(Entry<Value, Constraint<String>> l: solver.resultsAt(ret).entrySet()) {
////							System.err.print(l.getKey());
////							System.err.print("=");
////							System.err.println(l.getValue().toString(bodyTransformer.getFeatureNumberer()));
////						}
////						System.err.println();
////						System.err.println(Constraint.FACTORY.getCacheStats());
//						
////						IFDSTabulationProblem<Unit, ?, SootMethod> problem = new heros.problems.IFDSReachingDefinitions();
//						IFDSTabulationProblem<Unit, ?, SootMethod, ?> problem;
//						try {
//							Class<IFDSTabulationProblem<Unit, ?, SootMethod, ?>> clazz = (Class<IFDSTabulationProblem<Unit, ?, SootMethod, ?>>) Class.forName("heros.problems."+arg.analysisClassName);
//							Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
//							problem = (IFDSTabulationProblem<Unit, ?, SootMethod, ?>) constructor.newInstance( new JimpleBasedInterproceduralCFG() );
//						} catch (Exception e) {
//							throw new RuntimeException(e);
//						} 
//						
//						PerformanceTestDriver.perfTest(bodyTransformer, problem, javaProject, arg);
					}

				}));
			}
			
			beforeSootStart = System.currentTimeMillis();
			soot.Main.main(args);
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
	}

}
