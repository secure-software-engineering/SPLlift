package soot.spl.cflow;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Unit;
import soot.UnitBox;
import soot.spl.ifds.Constraint;
import soot.toolkits.graph.DirectedGraph;
import br.ufal.cideei.soot.instrument.FeatureTag;

//FIXME add additional "true"-edge from start to end?
public class LabeledUnitGraph implements LabeledDirectedGraph<Unit,Constraint<String>> {

	protected Map<Unit,Map<Unit,Constraint<String>>> unitToSuccToConstraint;
	protected Map<Unit,Map<Unit,Constraint<String>>> unitToPredToConstraint;
	protected final DirectedGraph<Unit> ug;
	
	public LabeledUnitGraph(DirectedGraph<Unit> unitGraph) {
		this.ug = unitGraph;
		computeSuccessorsForDisabledCase();
//		checkWellformedness();
	}
	
	private void checkWellformedness() {
		for(Unit u: this) {
			//no check for exit nodes
			if(getTails().contains(u)) continue;
			
			Constraint<String> succDisjunction = Constraint.falseValue();
			for(Unit succ: getSuccsOf(u)) {
				succDisjunction = succDisjunction.or(constraintOfEdge(u,succ));
			}
			Constraint<String> predDisjunction = getHeads().contains(u) ? Constraint.<String>trueValue() : Constraint.<String>falseValue();
			for(Unit pred: getPredsOf(u)) {
				predDisjunction = predDisjunction.or(constraintOfEdge(u,pred));
			}
			
			if(!predDisjunction.implies(succDisjunction).equals(Constraint.trueValue())) {
				throw new RuntimeException("Conditional CFG is not well formed! At Stmt "+u+" we get constraint "+succDisjunction);
			}
		}
	}
	
	private void computeSuccessorsForDisabledCase() {
		//initialize map
		unitToSuccToConstraint = new HashMap<Unit, Map<Unit,Constraint<String>>>();
		unitToPredToConstraint = new HashMap<Unit, Map<Unit,Constraint<String>>>();
		
		for(Unit u: this) {
			Map<Unit,Constraint<String>> succToConstraint = new HashMap<Unit, Constraint<String>>();
			unitToSuccToConstraint.put(u, succToConstraint);

			Set<Unit> jumpTargets = new HashSet<Unit>();
			for(UnitBox box: u.getUnitBoxes()) {				
				jumpTargets.add(box.getUnit());
			}
			Constraint<String> currConstraint = constraintOfNode(u);
			//for all jump targets
			for(Unit succ: jumpTargets) {
				Constraint<String> succConstraint = constraintOfNode(u);
				if(!currConstraint.implies(succConstraint).equals(Constraint.trueValue())) {
					throw new RuntimeException("Malformed program: "+u+" jumps to "+succ+" which has stronger constraints!");					
				}
			}
			//for the unique fall-through successor
			int count = 0;
			for(Unit succ: ug.getSuccsOf(u)) {
				if(jumpTargets.contains(succ)) continue;
				if(count>1) throw new InternalError("Multiple fall-through successors!?");
				count++;
				
				Constraint<String> succConstraint = constraintOfNode(succ);
				if(!currConstraint.implies(succConstraint).equals(Constraint.trueValue())) {
					Constraint<String> skipConstraint = currConstraint.and(succConstraint.not());
					Set<Unit> visited = new HashSet<Unit>();
					searchSuccessorsUnder(u,skipConstraint,succToConstraint,visited);
				}
			}
			
			
//			Map<Unit,Constraint<String>> succToConstraint = new HashMap<Unit, Constraint<String>>();
//			unitToSuccToConstraint.put(u, succToConstraint);
//			Constraint<String> constraint = constraintOfNode(u);
//			if(!constraint.equals(Constraint.trueValue())) {
//				Constraint<String> predConjunction = Constraint.trueValue();
//				for(Unit pred: unitGraph.getPredsOf(u)) {
//					predConjunction = predConjunction.and(constraintOfNode(pred));					
//				}
//				if(!predConjunction.implies(constraint).equals(Constraint.trueValue())) {
//					Set<Unit> visited = new HashSet<Unit>();
//					searchSuccessorsUnder(u,constraint.not(),succToConstraint,visited);
//				}
//			}
		}

		for(Unit u: this) {
			unitToPredToConstraint.put(u, new HashMap<Unit, Constraint<String>>());
		}		
		for(Unit u: this) {
			Map<Unit, Constraint<String>> map = unitToSuccToConstraint.get(u);
			for(Entry<Unit,Constraint<String>> entry: map.entrySet()) {
				unitToPredToConstraint.get(entry.getKey()).put(u, entry.getValue());
			}
		}
	}

	private void searchSuccessorsUnder(Unit u, Constraint<String> negatedConstraint, Map<Unit, Constraint<String>> succToConstraint, Set<Unit> visited) {
		visited.add(u);
		for(Unit succ: ug.getSuccsOf(u)) {
			Constraint<String> succConstraint = constraintOfNode(succ);
			Constraint<String> conjunction = succConstraint.and(negatedConstraint);
			if(conjunction.equals(Constraint.falseValue())) {
				if(!visited.contains(succ)) {
					searchSuccessorsUnder(succ, negatedConstraint, succToConstraint, new HashSet<Unit>(visited));
				}
			} else {
				succToConstraint.put(succ,conjunction);
			}
		}
	}

	@Override
	public List<Unit> getSuccsOf(Unit u) {
		Map<Unit, Constraint<String>> additionalSuccs = unitToSuccToConstraint.get(u);
		if(additionalSuccs.isEmpty())
			return ug.getSuccsOf(u);
		else {
			List<Unit> res = new ArrayList<Unit>(ug.getSuccsOf(u));
			res.addAll(additionalSuccs.keySet());
			return res;
		}
	}
	
	@Override
	public List<Unit> getPredsOf(Unit u) {
		Map<Unit, Constraint<String>> additionalPreds = unitToPredToConstraint.get(u);
		if(additionalPreds.isEmpty())
			return ug.getPredsOf(u);
		else {
			List<Unit> res = new ArrayList<Unit>(ug.getPredsOf(u));
			res.addAll(additionalPreds.keySet());
			return res;
		}	
	}
	

	@Override
	public Constraint<String> getLabel(Unit source, Unit target) {		
		return constraintOfEdge(source, target);
	}
	
	protected Constraint<String> constraintOfEdge(Unit n, Unit succ) {
		return constraintOfNode(n).and(constraintOfNode(succ));
	}

	private Constraint<String> constraintOfNode(Unit n) {
		FeatureTag tag = (FeatureTag) n.getTag(FeatureTag.FEAT_TAG_NAME);
		if(tag==null) {
			return Constraint.trueValue();
		} else {
			BitSet features = tag.getFeatureRep();
			return Constraint.make(features, true);
		}
	}

	@Override
	public List<Unit> getHeads() {
		return ug.getHeads();
	}

	@Override
	public List<Unit> getTails() {
		//do we want to filter here?
		return ug.getTails();
	}

	@Override
	public int size() {
		return ug.size();
	}

	@Override
	public Iterator<Unit> iterator() {
		return ug.iterator();
	}



}
