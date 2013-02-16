package soot.spl.ifds;

import heros.IFDSTabulationProblem;
import heros.solver.IFDSSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.javabdd.BDDFactory;

import org.eclipse.jdt.core.IJavaProject;

import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.options.Options;
import soot.util.NumberedString;
import br.ufal.cideei.handlers.AnalysisArgs;
import br.ufal.cideei.soot.instrument.FeatureModelInstrumentorTransformer;

public class PerformanceTestDriver {

	private static boolean checkResults = false;
	private static boolean debug = false;
	private static boolean crossChecksPassed = true;
	private static PrintWriter log;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected static void perfTest(FeatureModelInstrumentorTransformer bodyTransformer, IFDSTabulationProblem<Unit, ?, SootMethod,?> problem, IJavaProject javaProject, AnalysisArgs args) {
		long splDuration;
		
		Scene.v().setEntryPoints(Collections.singletonList(Scene.v().getMainMethod()));
		
		args.print();
		
		checkResults = args.checkResults;
		debug = args.debug;
	
		if(checkResults)
			System.err.println("Cross checks are enabled.");
		
		initNodeFactory();
		
		FeatureModelContext fmContext = new FeatureModelContext(bodyTransformer, javaProject);
		setupLog(fmContext, args);
		
		try {
			log("");
			log("");
			log("");
			log("New run for project;"+javaProject.getElementName());
			log("Analysis class;"+problem.getClass().getName());
			log("Main class;"+Scene.v().getMainClass().getName());
			log("Start time;"+new Date());
			log("JDK is "+ (Options.v().no_bodies_for_excluded() ? "not " : "") +"included in analysis");
			long durationSoot = System.currentTimeMillis()-Main.beforeSootStart;
			log("Soot startup and cg took;"+durationSoot);
			
			if(args.wait) {
				System.err.println("Waiting 10 seconds for eclipse to reach steady state...");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			}
			
			int reachableFeatures = bodyTransformer.getFeatureNumberer().size();
			log("Features reachable in code;"+reachableFeatures);
			
			Set<NumberedString> featuresReachableInCode = new HashSet<NumberedString>();
			for (NumberedString ns : bodyTransformer.getFeatureNumberer()) {
				featuresReachableInCode.add(ns);
			}

			Constraint<String> fullFeatureModelConstraint = args.useFeatureModel ?
					FMConstraintFactory.v().computeFeatureConstraint(fmContext) :
					Constraint.<String>trueValue();

			fmContext.setFullFMConstraint(fullFeatureModelConstraint);
					
			Set<NumberedString> allFeatures = new HashSet<NumberedString>();
			for (NumberedString ns : bodyTransformer.getFeatureNumberer()) {
				allFeatures.add(ns);
			}

			Constraint<String> featureModelConstraintForAnalysis = fullFeatureModelConstraint; 
			if(args.simplify) {
				featureModelConstraintForAnalysis = fullFeatureModelConstraint.simplify(allFeatures, featuresReachableInCode);
			}
			
			fmContext.setSimplifiedFMConstraint(featureModelConstraintForAnalysis);
			
			System.err.println("Full feature-model constraint:       "+fullFeatureModelConstraint);
			System.err.println("Simplified feature-model constraint: "+featureModelConstraintForAnalysis);
			
			log("Full feature-model node count;"+fullFeatureModelConstraint.size());
			log("Simplified feature-model node count;"+featureModelConstraintForAnalysis.size());

			if(featureModelConstraintForAnalysis.equals(Constraint.falseValue())) {
				System.err.println("Feature model not satisfiable. Exiting...");
				return;
			}
			
			int totalFeatures = bodyTransformer.getFeatureNumberer().size();
			log("Total features;"+totalFeatures);

			{
				System.err.println("Performing dry run to fully construct the ICFG upfront...");
				SPLIFDSSolver<Local> splSolver = new SPLIFDSSolver(problem,fmContext,args.eagerPruning);	
				splSolver.solve();
			}
			
			SPLIFDSSolver<Local> splSolver = new SPLIFDSSolver(problem,fmContext,args.eagerPruning);	
			System.err.println("Starting SPL solver...");
			long before = System.currentTimeMillis();
			splSolver.solve();
			splDuration = System.currentTimeMillis()-before;
	
			if(debug)
				printResults(bodyTransformer, splSolver);
			log("SPL solving took;"+splDuration);
			log("SPL flow functions constructed;"+splSolver.flowFunctionConstructionCount);
			log("SPL duration of flow function construction (phase 1);"+splSolver.durationFlowFunctionConstruction);
			log("SPL flow functions applied;"+splSolver.flowFunctionApplicationCount);
			log("SPL duration of flow function application (phase 2);"+splSolver.durationFlowFunctionApplication);
			log("SPL propagation count;"+splSolver.propagationCount);
			
			if(!checkResults) splSolver = null; //free memory

			if(!args.onlyLifted) {
				long totalDuration = 0L;
				long totalDurationCrossChecks = 0L;
				crossChecksPassed = true;
				final Iterable<Set<NumberedString>> powerSet = Powerset.powerset(featuresReachableInCode);
				int totalCombinations = (int) Math.pow(2, featuresReachableInCode.size());
				Iterator<BitSet> configIter;
				int numConfigs;
				if(args.determineValidConfigsUpfront) {
					int n = 0;
					List<BitSet> validConfigs = new LinkedList<BitSet>(); 
					System.err.println("Testing "+totalCombinations+" combinations for validity w.r.t. feature model.");
					long beforeCombinationTesting = System.currentTimeMillis();
					for(Set<NumberedString> subset: powerSet) {
						BitSet bs = new BitSet();
						for (NumberedString feature: subset) {
							bs.set(feature.getNumber());
						}
						
						if(args.useFeatureModel) {		
							Constraint<String> currentConfig = Constraint.make(bs, featuresReachableInCode);
							if(!featureModelConstraintForAnalysis.and(currentConfig).equals(Constraint.falseValue())) {
								validConfigs.add(bs);
							}
						} else {
							validConfigs.add(bs);
						}
						n++;
						if(n % 10000==0) System.err.println("So far tested "+n+" out of "+totalCombinations+".");
					}
					long durationCombinationTesting = System.currentTimeMillis()-beforeCombinationTesting;
					System.err.println("The feature model determined that "+validConfigs.size()+" configurations out of "+totalCombinations+" configurations for "+reachableFeatures+" reachable features are valid.");
					log("Checking for valid configs took;"+durationCombinationTesting);
					log("Number of valid configurations;"+validConfigs.size());
					configIter = validConfigs.iterator();
					numConfigs = validConfigs.size();
					System.err.println("Will now run separate analyses for "+numConfigs+" configurations.");
				} else {
					configIter = new Iterator<BitSet>() {
						Iterator<Set<NumberedString>> inner = powerSet.iterator();
						
						public boolean hasNext() {
							return inner.hasNext();
						}
						public BitSet next() {
							Set<NumberedString> next = inner.next();
							BitSet bs = new BitSet();
							for (NumberedString feature: next) {
								bs.set(feature.getNumber());
							}
							return bs;
						}
						
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
					numConfigs = totalCombinations;
					System.err.println("Will now run separate analyses for up to "+numConfigs+" configurations.");
				}
				
				int i=0;
				int numValid=0;
				long startTime = System.currentTimeMillis();
				long totalflowFunctionConstructionCount = 0;
				while(configIter.hasNext()) {
					BitSet bs = configIter.next();
					i++;
					
					boolean isValidConfig;
					if(args.determineValidConfigsUpfront) {
						isValidConfig = true;
					} else {
						Constraint<String> currentConfig = Constraint.make(bs, featuresReachableInCode);
						isValidConfig = !featureModelConstraintForAnalysis.and(currentConfig).equals(Constraint.falseValue());
					}
					
					if(isValidConfig) {
						System.err.print("Configuration "+i+", bitset:"+bs+",  "+((numConfigs-i)-1)+" more to go. ");
						numValid++;
						IFDSSolver<Unit, ?, SootMethod,?> solver = new SPLSingleConfigIFDSSolver(problem,bs);
						if(debug)
							System.err.println("Starting standard IFDS solver for configuration "+bs+"...");
						before = System.currentTimeMillis();
						solver.solve();
						long duration = System.currentTimeMillis()-before;
						log("Standard solving for configuration "+i+" took;"+duration);
						log("Standard flow functions constructed;"+solver.flowFunctionConstructionCount);
						totalDuration += duration;
						totalflowFunctionConstructionCount += solver.flowFunctionConstructionCount;
						log("Standard flow functions applied;"+solver.flowFunctionApplicationCount);
			
						if(debug)
							printIFDSResults(bodyTransformer, solver);
						
						if(checkResults) {
							long beforeCC = System.currentTimeMillis();
							checkResults(splSolver, solver, bs, featuresReachableInCode);
							long durationCC = System.currentTimeMillis()-beforeCC;
							totalDurationCrossChecks += durationCC;
						} 
						
						long delta = System.currentTimeMillis()-startTime;
						double estimatedDuration = (delta / (double)i) * numConfigs;
						long eta = startTime + (long) estimatedDuration;
						System.err.println("ETA: "+new Date(eta)+"   est. duration: "+(long) estimatedDuration);
					}
				}
				log("Number of valid configurations;"+numValid);
				log("TOTAL SEPARATE Solving took;"+totalDuration);
				log("TOTAL flow functions applied;"+totalflowFunctionConstructionCount);
				log("TOTAL SAVINGS;"+(totalDuration-splDuration));
				if(checkResults) {
					log("Cross checks took;"+totalDurationCrossChecks);
					if(crossChecksPassed)
						log("All cross checks passed.");
					else
						log("Some cross checks FAILED!!!");
				}			
			}
			
			log("End time;"+new Date());
		} finally {
			log.close();
		}
	}
    
    private static void log(String line) {
    	log.println(line);
    	log.flush();
    	if(debug)
    		System.err.println(line);
    }

	private static void setupLog(FeatureModelContext fmContext, AnalysisArgs args) {
		String type = args.analysisClassName;
		if(!args.useFeatureModel)
			type += "-nofm";
		else if(args.simplify)
			type += "-simplified";
		else
			type += "-full";
		try {
			String logFilePath = fmContext.getJavaProject().getResource().getLocation() + File.separator + "splAnalysis-"+type+".log";
			System.err.println("Writing log to: "+logFilePath);
			log = new PrintWriter(new FileOutputStream(new File(logFilePath),true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void initNodeFactory() {
		Constraint.FACTORY = BDDFactory.init(100000, 100000);
		Constraint.FACTORY.setVarNum(100); //some number high enough to accommodate the max. number of features; ideally we should compute this number 
	}

	private static void checkResults(SPLIFDSSolver<Local> splSolver, IFDSSolver<Unit, ?, SootMethod, ?> solver, BitSet enabledFeatures, Set<NumberedString> featuresReachableInCode) {
		Unit ret = Scene.v().getMainMethod().getActiveBody().getUnits().getLast();
		Map<?, Constraint<String>> splResults = splSolver.resultsAt(ret);
		Set<?> ifdsResults = solver.ifdsResultsAt(ret);
		Constraint<String> ifdsConstraint = Constraint.<String>make(enabledFeatures, featuresReachableInCode);
		for(Map.Entry<?, Constraint<String>> entry: splResults.entrySet()) {
			Object analysisResult = entry.getKey();
			Constraint<String> splConstraint = entry.getValue();
			if(!splConstraint.and(ifdsConstraint).equals(Constraint.falseValue())) {
				if(!ifdsResults.contains(analysisResult)) {
					System.err.println("!!! ERROR !!! SPL result too weak! IFDS="+ifdsResults+", SPL/IDE="+analysisResult);
					crossChecksPassed = false;
					return;
				}
			} 
		}
		for(Object res: ifdsResults) {
			Constraint<String> splConstraint = splResults.get(res);
			if(splConstraint==null) {
				System.err.println("!!! ERROR !!! SPL result too strong! IFDS="+ifdsResults+", SPL/IDE("+res+")={}");
				crossChecksPassed = false;
				return;
			} 
			if(splConstraint.and(ifdsConstraint).equals(Constraint.falseValue())) {
				System.err.println("!!! ERROR !!! SPL result too strong! IFDS="+ifdsResults+", SPL/IDE("+res+")="+splConstraint);
				crossChecksPassed = false;
				return;
			}
		}
		System.err.print("Cross checks passed. ");
	}

	protected static void printResults(FeatureModelInstrumentorTransformer bodyTransformer,
			SPLIFDSSolver<Local> splSolver) {
		for(SootMethod m: Scene.v().getMainClass().getMethods()) {
			if(!m.hasActiveBody()) continue;
			System.err.println(m.getActiveBody());
			Unit ret = m.getActiveBody().getUnits().getLast();
			for(Entry<Local, Constraint<String>> l: splSolver.resultsAt(ret).entrySet()) {
				System.err.print(l.getKey());
				System.err.print("=");
				System.err.println(l.getValue().toString(bodyTransformer.getFeatureNumberer()));
			}
			System.err.println();
		}
	}

	protected static void printIFDSResults(FeatureModelInstrumentorTransformer bodyTransformer,
			IFDSSolver<Unit, ?, SootMethod, ?> solver) {
		Unit ret = Scene.v().getMainMethod().getActiveBody().getUnits().getLast();
		for(Object l: solver.ifdsResultsAt(ret)) {
			System.err.println(l);
		}
		System.err.println();
	}
}
