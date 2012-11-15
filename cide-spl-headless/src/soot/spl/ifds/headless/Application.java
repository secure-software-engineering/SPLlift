package soot.spl.ifds.headless;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import br.ufal.cideei.handlers.AnalysisArgs;
import br.ufal.cideei.handlers.DoAnalysisOnClassPath;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		if(args.length<2 || args[0].equals("-help")) {
			System.out.println("USAGE: <projectname> <analysisclassname> <args>");
			System.out.println("Where <args> is a list of boolean flags enabled with +<arg>");
			System.out.println("and disabled with -<arg>. Available arguments and defaults:");
			AnalysisArgs defaultArgs = AnalysisArgs.headless("",Collections.<String>emptyList());
			defaultArgs.print();
			return IApplication.EXIT_OK;
		}

		String projectName = args[0];
		String analysisClassName = args[1]; 
		
		List<String> argVals = new LinkedList<String>();
		for (int i = 2; i < args.length; i++) {
			String arg = args[i];
			if(arg.startsWith("+") || arg.startsWith("-"))
				argVals.add(arg);
			else
				System.err.println("Ignoring invalid argument: "+arg);
		}

		System.out.println("Applying analysis to project: " + projectName);

		try {
			// Import project description:
			// IWorkspace workspace = ResourcesPlugin.getWorkspace();
			// System.err.println(workspace.getRoot().getLocation());
			// IProjectDescription description =
			// workspace.loadProjectDescription(new
			// Path(projectPath).append(".project"));
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			project.open(null);
			IJavaProject javaProject = JavaCore.create(project);
			
			AnalysisArgs arg = AnalysisArgs.headless(analysisClassName,argVals);
			
			DoAnalysisOnClassPath.applyToProject(javaProject, arg);
		} catch (Exception e) {
			System.err.println("Error loading project " + projectName);
			System.err.println(e.getLocalizedMessage());
		}

		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}
}