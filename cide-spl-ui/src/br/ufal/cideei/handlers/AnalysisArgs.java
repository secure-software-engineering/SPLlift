package br.ufal.cideei.handlers;

import java.lang.reflect.Field;
import java.util.List;

public class AnalysisArgs {
	
	public boolean wait;
	
	public boolean includeJDK;
	
	public boolean simplify = true;
	
	public boolean useFeatureModel = true;
	
	public boolean eagerPruning = true;
	
	public boolean checkResults;
	
	public boolean debug = false;
	
	public boolean j2me;
	
	public boolean onlyLifted = true;
	
	public boolean determineValidConfigsUpfront;

	public String analysisClassName = "IFDSLocalInfoFlow"; //"IFDSUninitializedVariables";
	
	private AnalysisArgs(boolean headless) {
		//set general defaults
		simplify = true;			
		if(headless) {
			//set defaults for headless build
			wait = true;
			includeJDK = true;
		} else {
			checkResults = true;
		}
	}
	
	public static AnalysisArgs headless(String analysisClassName, List<String> argVals) {
		AnalysisArgs args = new AnalysisArgs(true);
		args.analysisClassName = analysisClassName;
		
		for (String arg: argVals) {
			String theArg = arg.substring(1);
			switch(arg.charAt(0)) {
			case '+':
				args.set(theArg,true);
				break;
			case '-':
				args.set(theArg,false);
				break;
			default: throw new IllegalStateException();
			}
		}
		
		return args;
	}
	
	private void set(String argument, boolean overrideValue) {
        try {
            Field f = AnalysisArgs.class.getField(argument);
            f.setBoolean(this, overrideValue);
        }
        catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                    "No such option: "+argument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	public static AnalysisArgs interactive() {
		return new AnalysisArgs(false);
	}
	
	public void print() {
		System.err.print("Arguments: ");
        try {
            Field[] fields = AnalysisArgs.class.getDeclaredFields();
            for (Field field : fields) {
            	System.err.print(field.getName()+"="+field.get(this)+" ");
			}
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		System.err.println("");
	}
}
