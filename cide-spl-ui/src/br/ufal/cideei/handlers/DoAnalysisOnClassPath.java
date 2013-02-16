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

package br.ufal.cideei.handlers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import soot.G;
import soot.spl.ifds.Main;
import br.ufpe.cin.emergo.properties.DFA4SPLProperties;

//TODO: this class is very similar to DoFeatureObliviousAnalysisOnClassPath. Check for common funcionality and for code reuse opportunities.
/**
 * Invokes feature-sensitive analyses on a Eclipse project. Mainly for collecting data/metrics.
 * 
 * @author T�rsis
 */
public class DoAnalysisOnClassPath extends AbstractHandler {
	// #ifdef METRICS
//@	private static MetricsSink sink;
//@
	// #endif

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IJavaProject) {
			IJavaProject javaProject = (IJavaProject) firstElement;

			applyToProject(javaProject, AnalysisArgs.interactive());
		}		
		else if(firstElement instanceof ICompilationUnit) {
			ICompilationUnit cu = (ICompilationUnit) firstElement;
			IType type = cu.findPrimaryType();
			IJavaProject javaProject = cu.getJavaProject();
			if(type!=null) {
				try {
					String mainClass = type.getFullyQualifiedName();
					Main.main(getSootClasspath(javaProject), mainClass, javaProject, AnalysisArgs.interactive());
				} finally {
					G.reset();
				}
			}
		}
		
		
//		//#ifdef CACHEPURGE
////@		br.Main.randomLong();
//		//#endif
//		
//		// TODO: exteriorize this number as a configuration parameter. Abstract away the looping.
//		int times = 1;
//		try {
//			for (int i = 0; i < times; i++) {
//				// #ifdef METRICS
////@				sink = new MetricsSink(new MetricsTable(new File(System.getProperty("user.home") + File.separator + "fs.xls")));
//				// #endif
//
//				IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
//				Object firstElement = selection.getFirstElement();
//				if (firstElement instanceof IJavaProject) {
//					IJavaProject javaProject = (IJavaProject) firstElement;
//
//					IClasspathEntry[] classPathEntries = null;
//					try {
//						classPathEntries = javaProject.getResolvedClasspath(true);
//					} catch (JavaModelException e) {
//						e.printStackTrace();
//						throw new ExecutionException("No source classpath identified");
//					}
//
//					/*
//					 * To build the path string variable that will represent Soot's classpath we will first iterate
//					 * through all libs (.jars) files, then through all source classpaths.
//					 * 
//					 * FIXME: WARNING: A bug was found on Soot, in which the FileSourceTag would contain incorrect
//					 * information regarding the absolute location of the source file. In this workaround, the classpath
//					 * must be injected into the FeatureModelInstrumentorTransformer class (done through its
//					 * constructor).
//					 * 
//					 * As a consequence, we CANNOT build an string with all classpaths that contains source code for the
//					 * project and thus one only source code classpath can be analysed at a given time.
//					 * 
//					 * This seriously restricts the range of projects that can be analysed with this tool.
//					 */
//					StringBuilder libsPaths = new StringBuilder();
//					for (IClasspathEntry entry : classPathEntries) {
//						if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
//							File file = entry.getPath().makeAbsolute().toFile();
//							if (file.isAbsolute()) {
//								libsPaths.append(file.getAbsolutePath() + File.pathSeparator);
//							} else {
//								libsPaths.append(ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString() + File.pathSeparator);
//							}
//						}
//					}
//					for (IClasspathEntry entry : classPathEntries) {
//						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
//							this.addPacks(javaProject, entry, libsPaths.toString());
//						}
//					}
//				}
//				// Resets SOOT
//				SootManager.reset();
//
//				/*
//				 * terminate the Metrics Facade. This dumps all the in-memory information.
//				 */
//				// #ifdef METRICS
////@				sink.terminate();
////@				sink = null;
//				// #endif
//				System.out.println("=============" + (i + 1) + "/" + times + "=============");
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		} finally {
//			SootManager.reset();
//			// #ifdef METRICS
////@			sink.terminate();
//			// #endif
//		}

		return null;
	}

	public static void applyToProject(IJavaProject javaProject, AnalysisArgs args) {
		try {
			String mainClass = DFA4SPLProperties.getMainClass(javaProject.getResource());
			Main.main(getSootClasspath(javaProject), mainClass, javaProject, args);
		} finally {
			G.reset();
		}
	}
	
	public static URL[] projectClassPath(IJavaProject javaProject) {
		IClasspathEntry[] cp;
		try {
			cp = javaProject.getResolvedClasspath(true);
			List<URL> urls = new ArrayList<URL>();
//			String uriString = ResourcesPlugin.getWorkspace().getRoot().getFile(
//					javaProject.getOutputLocation()).getLocationURI().toString()
//					+ "/";
//			urls.add(new URI(uriString).toURL());
			for (IClasspathEntry entry : cp) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath());
				File f;
				if(file==null) {
					f = entry.getPath().toFile();
				} else {
					IPath location = file.getLocation();
					if(location!=null)
						f = location.toFile();
					else
						f = file.getFullPath().toFile();
				}
				URL url = f.toURI().toURL();
				urls.add(url);
			}
			URL[] array = new URL[urls.size()];
			urls.toArray(array);
			return array;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return new URL[0];
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return new URL[0];
		} 
	}
	
	public static String getSootClasspath(IJavaProject javaProject) {
		return urlsToString(projectClassPath(javaProject));
	}

	public static String urlsToString(URL[] urls) {
		StringBuffer cp = new StringBuffer();
		for (URL url : urls) {
			cp.append(url.getPath());
			cp.append(File.pathSeparator);
		}
		
		return cp.toString();
	}
}
