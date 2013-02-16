package br.ufpe.cin.dfa4spl.plverifier.alloy;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.dfa4spl.plverifier.alloy.io.CannotReadAlloyFileException;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprCall;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class AlloyNodes {

	private A4Reporter rep;
	private CompModule module;
	
	private static final String PRED_PREFIX = "<b>pred</b> this/";
	private static final String SIG_PREFIX =  "<b>sig</b> this/";
	private static final String SIG_SUFFIX =  " <i>{this/Bool}</i>";
	
	public AlloyNodes(String filePath) throws CannotReadAlloyFileException {
		rep = new A4Reporter() {
			// For example, here we choose to display each "warning" by printing it to System.out
			@Override
			public void warning(ErrorWarning msg) {
				System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
				System.out.flush();
			}
		};
		
		try {
			module = CompUtil.parseEverything_fromFile(rep, null, filePath);
		} catch (Err e) {
			throw new CannotReadAlloyFileException("File: " + filePath, e);
		}
	}

	public Expr makeAND(List<Expr> sigNames) {
		List<Expr> args = new ArrayList<Expr>();
		args.add(sigNames.get(0));

		if (sigNames.size() == 1) {
			return sigNames.get(0);
		} else {
			Expr expressionCall = sigNames.remove(0);
			return ExprBinary.Op.AND.make(Pos.UNKNOWN, Pos.UNKNOWN, expressionCall, makeAND(sigNames));
		}
	}

	public Expr makeOR(List<Expr> sigNames) {
		List<Expr> args = new ArrayList<Expr>();
		args.add(sigNames.get(0));

		if (sigNames.size() == 1) {
			return sigNames.get(0);
		} else {
			Expr expressionCall = sigNames.remove(0);
			return ExprBinary.Op.OR.make(Pos.UNKNOWN, Pos.UNKNOWN, expressionCall, makeOR(sigNames));
		}
	}

	public void setFuncBody(Func func, Expr newBody) {
		try {
			func.setBody(newBody);
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	public Func getFunc(String funcName) throws CannotFindFunc {
		SafeList<Func> allFunc = module.getAllFunc();
		Func resultFunc = null;
		
		for (Func func : allFunc) {
			if (func.getDescription().equals(AlloyNodes.PRED_PREFIX + funcName)) {
				resultFunc = func;
			}
		}
		
		if (resultFunc == null) {
			throw new CannotFindFunc("Func name: " + funcName);
		}
		
		return resultFunc;
	}

	public Sig getBooleanSig(String booleanSigName) throws CannotFindBooleanSig {
		SafeList<Sig> allSigs = module.getAllSigs();
		Sig resultSig = null;
		
		for (Sig sig : allSigs) {
			if (sig.getDescription().equals(AlloyNodes.SIG_PREFIX + booleanSigName + AlloyNodes.SIG_SUFFIX)) {
				resultSig = sig;
			}
		}
		
		if (resultSig == null) {
			throw new CannotFindBooleanSig("Boolean sig name: " + booleanSigName);
		}
		
		return resultSig;
	}

	public boolean executeCommand(String commandName, String type) {
		boolean result = false;
		
		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.SAT4J;
		
		for (Command command : module.getAllCommands()) {
			// Execute the command
			A4Solution ans = null;
			try {
				ans = TranslateAlloyToKodkod.execute_command(rep, module.getAllReachableSigs(), command, options);
			} catch (Err e) {
				e.printStackTrace();
			}
			
			// If satisfiable...
			if (ans.satisfiable()) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
}