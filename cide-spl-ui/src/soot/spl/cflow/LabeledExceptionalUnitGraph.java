package soot.spl.cflow;

import java.util.BitSet;

import soot.Body;
import soot.Unit;
import soot.spl.ifds.Constraint;
import soot.toolkits.graph.ExceptionalUnitGraph;
import br.ufal.cideei.soot.instrument.FeatureTag;

public class LabeledExceptionalUnitGraph extends ExceptionalUnitGraph implements LabeledDirectedGraph<Unit,Constraint<String>> {

	public LabeledExceptionalUnitGraph(Body body) {
		super(body);
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



}
