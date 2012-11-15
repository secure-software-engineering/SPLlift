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

package br.ufal.cideei.soot;

import soot.G;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import br.ufal.cideei.features.IFeatureExtracter;

public class SootManager {
	public static void configure(String classpath) {
		Options.v().set_allow_phantom_refs(true);

		Options.v().set_verbose(false);
		Options.v().set_keep_line_number(true);
		Options.v().set_src_prec(Options.src_prec_java);
		Options.v().set_soot_classpath(classpath);
		Options.v().set_prepend_classpath(true);
		PhaseOptions.v().setPhaseOption("bb", "off");
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
		PhaseOptions.v().setPhaseOption("jj.a", "on");
		PhaseOptions.v().setPhaseOption("jj.ule", "on");
		// Options.v().set_whole_program(true);
		// PhaseOptions.v().setPhaseOption("cg.cha", "off");
		// PhaseOptions.v().setPhaseOption("cg.paddle", "on");
	}

	public static void runPacks(IFeatureExtracter extracter) {
		PackManager.v().runPacks();		
	}

	public static void reset() {
		G.reset();
	}

	public static Scene getScene() {
		return Scene.v();
	}

	public static SootClass loadAndSupport(String className) {
		SootClass sootClass = Scene.v().loadClassAndSupport(className);
		sootClass.setApplicationClass();
		return sootClass;
	}

	public static SootMethod getMethodByName(String className, String methodIdentifier) {
		if (!Scene.v().containsClass(className)) {
			loadAndSupport(className);
		}
		return Scene.v().getSootClass(className).getMethodByName(methodIdentifier);
	}

	public static SootMethod getMethodBySignature(String className, String signature) {
		if (!Scene.v().containsClass(className)) {
			loadAndSupport(className);
		}
		return Scene.v().getSootClass(className).getMethod(signature);
	}

}
