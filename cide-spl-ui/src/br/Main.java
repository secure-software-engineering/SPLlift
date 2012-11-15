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

package br;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.InitAnalysis;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.util.Chain;
import soot.util.JasminOutputStream;

public class Main {

	// #ifdef CACHEPURGE
//@	private static long[] wasteOfSpace = new long[131072]; // 131072*64 bits =
//@															// 8388608 bits =
//@															// 8MB
//@	private static Random rdm = new Random();
//@	private static long acc = 0;
//@
//@	static {
//@		for (int index = 0; index < wasteOfSpace.length; index++) {
//@			wasteOfSpace[index] = rdm.nextLong();
//@		}
//@	}
//@
//@	public static long randomLong() {
//@		return wasteOfSpace[rdm.nextInt(wasteOfSpace.length)];
//@	}
//@
	// #endif

	public static void main(String[] args) throws IOException {

		Options.v().set_whole_program(true);
		Options.v().set_verbose(false);

		Scene scene = Scene.v();
		SootClass sClass;
		SootMethod method;

		// Carregar depend�ncias e a raiz Object
		SootClass objClass = scene.loadClassAndSupport("java.lang.Object");
		scene.loadClassAndSupport("java.lang.System");

		// Declarar a classe como public
		sClass = new SootClass("HelloWorld", Modifier.PUBLIC);

		/*
		 * � obrigat�rio definir a superclasse, pois quando ela n�o � definida,
		 * o compilador se encarrega de faz�-lo, mas nesse caso � obrigat�rio
		 * explicit�-la.
		 */
		sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		scene.addClass(sClass);

		/*
		 * Criar a assinatura do m�todo(nome, par�metros e retorno):
		 * 
		 * public static void main(String[])
		 * 
		 * o corpo ser� definido mais abaixo
		 */
		method = new SootMethod("main", Arrays.asList(new Type[] { ArrayType.v(
				RefType.v("java.lang.String"), 1) }), VoidType.v(),
				Modifier.PUBLIC | Modifier.STATIC);

		sClass.addMethod(method);

		// Este bloco � utilizado para definir o corpo do m�todo
		{
			// Um Body s� deve ser instanciado relacionando-o diretamente a uma
			// IR
			JimpleBody body = Jimple.v().newBody(method);

			method.setActiveBody(body);
			Chain<Local> locals = body.getLocals();
			Chain<Unit> units = body.getUnits();
			Local arg, tmpRef;

			// Add some locals, java.lang.String l0
			arg = Jimple.v().newLocal("l0",
					ArrayType.v(RefType.v("java.lang.String"), 1));
			locals.add(arg);

			// Add locals, java.io.printStream tmpRef
			tmpRef = Jimple.v().newLocal("tmpRef",
					RefType.v("java.io.PrintStream"));
			locals.add(tmpRef);

			// add "l0 = @parameter0"
			units.add(Jimple.v().newIdentityStmt(
					arg,
					Jimple.v().newParameterRef(
							ArrayType.v(RefType.v("java.lang.String"), 1), 0)));

			// add "tmpRef = java.lang.System.out"
			Unit tmpRefAssignUnit = Jimple
					.v()
					.newAssignStmt(
							tmpRef,
							Jimple
									.v()
									.newStaticFieldRef(
											Scene
													.v()
													.getField(
															"<java.lang.System: java.io.PrintStream out>")
													.makeRef()));
			units.add(tmpRefAssignUnit);

			// insert "tmpRef.println("Hello world!")"
			{
				SootMethod toCall = Scene
						.v()
						.getMethod(
								"<java.io.PrintStream: void println(java.lang.String)>");
				units.add(Jimple.v().newInvokeStmt(
						Jimple.v().newVirtualInvokeExpr(tmpRef,
								toCall.makeRef(),
								StringConstant.v("Hello world!"))));
			}

			// insert "return"
			units.add(Jimple.v().newReturnVoidStmt());

			{
				UnitGraph unitGraph = new BriefUnitGraph(body);
				SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);

				SimpleLocalUses localUses = new SimpleLocalUses(unitGraph,
						localDefs);
				List localUsesOfTmpRefValueBoxPair = localUses
						.getUsesOf(tmpRefAssignUnit);

				SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);
				List liveLocalsBefore = liveLocals
						.getLiveLocalsAfter(tmpRefAssignUnit);

				scene.setMainClass(sClass);
				scene.loadNecessaryClasses();

				InitAnalysis init = new InitAnalysis(unitGraph);
				for (Unit unit : units) {
					System.out.println(init.getFlowAfter(unit));
				}

				// CHATransformer.v().transform();
				// CallGraph cg = Scene.v().getCallGraph();

				// ReachingDefs reachingDefs = new ReachingDefs(unitGraph);
				// System.out.println(reachingDefs);

			}
		}

		// String fileName = SourceLocator.v().getFileNameFor(sClass,
		// Options.output_format_class);
		String fileName = SourceLocator.v().getSourceForClass(sClass.getName());
		System.out.println(fileName);
		OutputStream streamOut = new JasminOutputStream(new FileOutputStream(
				fileName));
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(
				streamOut));
		JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
		jasminClass.print(writerOut);
		writerOut.flush();
		streamOut.close();

	}

	// #ifdef CACHEPURGE
//@	public static void waste() {
//@		// reads cells from the useless array and do some useless calculations
//@		long acc = 0;
//@		boolean bool = rdm.nextBoolean();
//@		for (int i = 0; i < wasteOfSpace.length; i++) {
//@			if (bool) {
//@				bool = !bool;
//@				acc += wasteOfSpace[i];
//@			} else {
//@				bool = !bool;
//@				acc -= wasteOfSpace[i];
//@			}
//@		}
//@		Main.acc = acc;
//@	}
	// #endif

}
