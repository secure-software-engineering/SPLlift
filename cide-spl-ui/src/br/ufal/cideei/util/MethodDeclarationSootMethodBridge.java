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

package br.ufal.cideei.util;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

// TODO: Auto-generated Javadoc
/**
 * The Class MethodDeclarationSootMethodBridge.
 */
public class MethodDeclarationSootMethodBridge {

	/** The method declaration. */
	private MethodDeclaration methodDeclaration;

	/**
	 * Disabled default constructor.
	 */
	@SuppressWarnings("unused")
	private MethodDeclarationSootMethodBridge() {
	}

	/**
	 * Instantiates a new method declaration soot method bridge.
	 * 
	 * @param methodDeclaration
	 *            the method declaration
	 */
	public MethodDeclarationSootMethodBridge(MethodDeclaration methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}

	/**
	 * Gets the soot method sub signature.
	 * 
	 * @return the soot method sub signature
	 */
	public String getSootMethodSubSignature() {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] argumentsBinding = methodBinding.getParameterTypes();
		ITypeBinding returnBinding = methodBinding.getReturnType();

		StringBuilder stringMethodBuilder = new StringBuilder();

		stringMethodBuilder.append(returnBinding.getQualifiedName());
		stringMethodBuilder.append(" ");
		if (methodDeclaration.isConstructor()) {
			stringMethodBuilder.append("<init>");
		} else {
			stringMethodBuilder.append(methodBinding.getName());
		}
		stringMethodBuilder.append("(");

		for (int index = 0; index < argumentsBinding.length; index++) {
			stringMethodBuilder.append(argumentsBinding[index].getQualifiedName());
			if (!(index == argumentsBinding.length - 1)) {
				stringMethodBuilder.append(",");
			}
		}

		stringMethodBuilder.append(")");
		return stringMethodBuilder.toString();
	}

	/**
	 * Gets the soot method signature.
	 * 
	 * @return the soot method signature
	 */
	public String getSootMethodSignature() {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] argumentsBinding = methodBinding.getParameterTypes();
		ITypeBinding declaringTypeBinding = methodBinding.getDeclaringClass();
		ITypeBinding returnBinding = methodBinding.getReturnType();

		StringBuilder stringMethodBuilder = new StringBuilder("<");
		stringMethodBuilder.append(declaringTypeBinding.getQualifiedName());
		stringMethodBuilder.append(": ");
		stringMethodBuilder.append(returnBinding.getQualifiedName());
		stringMethodBuilder.append(" ");
		stringMethodBuilder.append(methodBinding.getName());
		stringMethodBuilder.append("(");

		for (int index = 0; index < argumentsBinding.length; index++) {
			stringMethodBuilder.append(argumentsBinding[index].getQualifiedName());
			if (!(index == argumentsBinding.length - 1)) {
				stringMethodBuilder.append(",");
			}
		}

		stringMethodBuilder.append(")>");
		return stringMethodBuilder.toString();
	}

	/**
	 * Gets the correspondent classpath.
	 * 
	 * @param file
	 *            the file
	 * @return the correspondent classpath
	 * @throws ExecutionException
	 *             the execution exception
	 */
	public static String getCorrespondentClasspath(IFile file) throws ExecutionException {
		/*
		 * used to find out what the classpath entry related to the IFile of the
		 * text selection. this is necessary for some algorithms that might use
		 * the Soot framework
		 */
		IProject project = file.getProject();
		IJavaProject javaProject = null;

		try {
			if (file.getProject().isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				javaProject = JavaCore.create(project);
			}
		} catch (CoreException e) {
			e.printStackTrace();
			throw new ExecutionException("Not a Java Project");
		}

		/*
		 * When using the Soot framework, we need the path to the package root
		 * in which the file is located. There may be other ways to acomplish
		 * this.
		 */
		String pathToSourceClasspathEntry = null;

		IClasspathEntry[] classPathEntries = null;
		try {
			classPathEntries = javaProject.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException("No source classpath identified");
		}
		for (IClasspathEntry entry : classPathEntries) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				pathToSourceClasspathEntry = ResourcesPlugin.getWorkspace().getRoot().getFile(entry.getPath()).getLocation().toOSString();
				break;
			}
		}

		return pathToSourceClasspathEntry;
	}

	/**
	 * Gets the parent method.
	 * 
	 * @param node
	 *            the node
	 * @return the parent method
	 */
	public static MethodDeclaration getParentMethod(ASTNode node) {
		if (node == null) {
			return null;
		} else {
			if (node.getNodeType() == ASTNode.METHOD_DECLARATION) {
				return (MethodDeclaration) node;
			} else {
				return getParentMethod(node.getParent());
			}
		}
	}
}
