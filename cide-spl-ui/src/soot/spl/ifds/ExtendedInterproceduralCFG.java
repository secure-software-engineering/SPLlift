package soot.spl.ifds;

import heros.InterproceduralCFG;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.ThrowStmt;
import soot.tagkit.TryCatchTag;

public class ExtendedInterproceduralCFG implements InterproceduralCFG<Unit,SootMethod> {
	
	protected InterproceduralCFG<Unit,SootMethod> delegate;

	public ExtendedInterproceduralCFG(InterproceduralCFG<Unit, SootMethod> delegate) {
		this.delegate = delegate;
	}

	public List<Unit> getSuccsOf(Unit n) {
		List<Unit> succsOf = delegate.getSuccsOf(n);
		if(n instanceof ThrowStmt) {
			TryCatchTag tryCatchTag = (TryCatchTag) getMethodOf(n).getActiveBody().getTag(TryCatchTag.NAME);
			if(tryCatchTag!=null) {
				for(Unit possibleHandlerUnit: succsOf) {
					Unit fallThroughUnit = tryCatchTag.getFallThroughUnitOf(possibleHandlerUnit);
					if(fallThroughUnit!=null) {
						List<Unit> res = new LinkedList<Unit>(succsOf);
						res.add(fallThroughUnit);
						return res;
					}
				}
			} 
		}
		return succsOf;
	}
	
	public boolean isFallThroughSuccessor(Unit stmt, Unit succ) {
		if(delegate.isFallThroughSuccessor(stmt, succ))
			return true;
		if(stmt instanceof ThrowStmt) {
			TryCatchTag tryCatchTag = (TryCatchTag) getMethodOf(stmt).getActiveBody().getTag(TryCatchTag.NAME);
			if(tryCatchTag!=null) {
				for(Unit possibleHandlerUnit: delegate.getSuccsOf(stmt)) {
					Unit fallThroughUnit = tryCatchTag.getFallThroughUnitOf(possibleHandlerUnit);
					if(fallThroughUnit==succ) {
						return true;
					}
				}
			}
		}
		return false;		
	}

	public boolean isBranchTarget(Unit stmt, Unit succ) {
		if(delegate.isBranchTarget(stmt, succ))
			return true;
		if(stmt instanceof ThrowStmt) {
			for(Trap trap: delegate.getMethodOf(stmt).getActiveBody().getTraps()) {
				if(trap.getHandlerUnit()==succ) {
					return true;
				}
			}
		} 
		return false;		
	}
	
	public SootMethod getMethodOf(Unit n) {
		return delegate.getMethodOf(n);
	}

	public Set<SootMethod> getCalleesOfCallAt(Unit n) {
		return delegate.getCalleesOfCallAt(n);
	}

	public Set<Unit> getCallersOf(SootMethod m) {
		return delegate.getCallersOf(m);
	}

	public Set<Unit> getCallsFromWithin(SootMethod m) {
		return delegate.getCallsFromWithin(m);
	}

	public Set<Unit> getStartPointsOf(SootMethod m) {
		return delegate.getStartPointsOf(m);
	}

	public List<Unit> getReturnSitesOfCallAt(Unit n) {
		return delegate.getReturnSitesOfCallAt(n);
	}

	public boolean isCallStmt(Unit stmt) {
		return delegate.isCallStmt(stmt);
	}

	public boolean isExitStmt(Unit stmt) {
		return delegate.isExitStmt(stmt);
	}

	public boolean isStartPoint(Unit stmt) {
		return delegate.isStartPoint(stmt);
	}

	public Set<Unit> allNonCallStartNodes() {
		return delegate.allNonCallStartNodes();
	}
	
}
