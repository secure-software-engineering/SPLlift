package soot.spl.cflow.test;

import java.util.ArrayList;
import java.util.List;

import soot.spl.ifds.Constraint;

public class AugmentedLabeledDirectedGraphImpl<N extends Node, L extends Constraint<?>> extends LabeledDirectedGraphImpl<N, L> {
	
	@Override
	public List<N> getPredsOf(N n) {
		if(getTails().contains(n)) {
			List<N> res = new ArrayList<N>(super.getPredsOf(n));
			res.addAll(getHeads());
			return res;
		}		
		return super.getPredsOf(n);
	}

	@Override
	public List<N> getSuccsOf(N n) {
		if(getHeads().contains(n)) {
			List<N> res = new ArrayList<N>(super.getSuccsOf(n));
			res.addAll(getTails());
			return res;
		}		
		return super.getSuccsOf(n);
	}
	
	@SuppressWarnings("unchecked")
	public L getLabel(N source, N target) {
		if(getHeads().contains(source) && getTails().contains(target))
			return (L) Constraint.trueValue();
		
		return super.getLabel(source, target);
	}	
}
