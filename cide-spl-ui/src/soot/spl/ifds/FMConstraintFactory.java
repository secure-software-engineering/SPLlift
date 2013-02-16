package soot.spl.ifds;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import org.prop4j.And;
import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;

import soot.G;
import soot.toolkits.scalar.Pair;
import soot.util.NumberedString;
import soot.util.StringNumberer;
import config.gen.CombinationFactory;
import config.gen.CombinationFilter;
import config.gen.Configurations;
import featureide.fm.core.Feature;
import featureide.fm.core.FeatureModel;
import featureide.fm.core.io.UnsupportedModelException;
import featureide.fm.core.io.guidsl.FeatureModelReader;

public class FMConstraintFactory {

	protected BDDFactory f;
	protected String projectName;
	protected StringNumberer featureNumberer;
	
	/**
	 * Builds a Constraint that represents the feature model.
	 * 
	 * @param fmContext
	 *            the feature model context information
	 * @param performTest
	 *            flag indicating the execution of the test
	 * @return the Constraint representing the feature model
	 */
	public Constraint<String> computeFeatureConstraint(FeatureModelContext fmContext) {
		Set<NumberedString> usedFeatures = new HashSet<NumberedString>();
		for (NumberedString ns : fmContext.getFeatureNumberer()) {
			usedFeatures.add(ns);
		}
		
		projectName = fmContext.getJavaProject().getElementName();
		f = Constraint.FACTORY;
		featureNumberer = fmContext.getFeatureNumberer();
		
		// TODO cache the resulting BDD
		BDD fmBDD = null;
		
		File modelFile = new File(fmContext.getJavaProject().getResource().getLocation() + File.separator + "model.m");
		FeatureModel featureModel = new FeatureModel();
		FeatureModelReader reader = new FeatureModelReader(featureModel);
		try {
			reader.readFromFile(modelFile);
		} catch (UnsupportedModelException e) {
			throw new RuntimeException("There was a problem parsing the feature model " + modelFile);
		} catch (FileNotFoundException e) {
			G.v().out.println("No feature model found");
			return Constraint.trueValue();
		}
		
		/*
		 *  XXX the following line raises the exception: 
		 *  net.sf.javabdd.JFactory$JavaBDDException: Trying to decrease the number of variables
		 */
		// Collection<Feature> features = featureModel.getFeatures();
		// f.setVarNum(features.size() + 1);
		
		fmBDD = buildBDD(featureModel.getRoot(), featureModel.getConstraints());
		
		return Constraint.make(fmBDD);
	}

	/**
	 * Builds a BDD representation from the feature model by iterating over the
	 * tree (@{Code Feature root}) and the Constraints and then joining them
	 * together.
	 * 
	 * @param list
	 * 
	 * @param root 
	 * 			the root feature of the feature model.
	 * @return the BDD representing the feature model.
	 */
	private BDD buildBDD(Feature root, List<featureide.fm.core.Constraint> list) {
		assert (root != null);
		
		// handle FM constraints
		BDD constraintsBDD = buildBDDFromConstraints(list);
		
		// handle FM tree constraints
		BDD treeBDD = buildBDDFromTree(root);
		
		/*
		 *  handle case (i) of FM transformation (see internalBuildBDD method javadoc) and
		 *  delegates the rest of the cases to the buildBDDFromTree method.
		 */
		BDD rootBDD = bdd(root.getName());
		
		if (treeBDD == null)
			treeBDD = rootBDD;
		else
			treeBDD.andWith(rootBDD);
		
		if (constraintsBDD != null)
			treeBDD.andWith(constraintsBDD);
		
		return treeBDD;
	}

	/**
	 * Builds a BDD based on the list of constraints. 
	 * 
	 * Returns null if the constraint list is empty.
	 * 
	 * @param constraints
	 * @return the BDD representing the list of constraints, null if there are no constraints.
	 */
	private BDD buildBDDFromConstraints(List<featureide.fm.core.Constraint> constraints) {
		BDD constraintBDD = null;
		for (featureide.fm.core.Constraint constraint : constraints) {
			Node node = constraint.getNode();
			if (constraintBDD == null) {
				constraintBDD = transform(node);
			} else {
				constraintBDD.andWith(transform(node));
			}
		}
		return constraintBDD;		
	}

	/**
	 * Recursively navigate though every node and builds a BDD from the @{code
	 * node}.
	 * 
	 * Only supports operators of types {@code And}, {@code Implies},
	 * {@code Equals}, {@code Not} and {@code Or} and literal of type
	 * {@code Literal}.
	 * 
	 * @param node
	 *            the representation of a constraint
	 * @return the BDD representing the constraint, null if one of operators is
	 *         not supported.
	 */
	private BDD transform(Node node) {
		// When adding new handlers to other types, don't forget to update the javadoc.
		if (node instanceof Literal) {
			return bdd(((Literal) node).var.toString());
		} else if (node instanceof And) {
			return transform(node.getChildren()[0]).andWith(transform(node.getChildren()[1]));
		} else if (node instanceof Implies) {
			return transform(node.getChildren()[0]).impWith(transform(node.getChildren()[1]));
		} else if (node instanceof Not) {
			return bdd(node.getChildren()[0].toString()).not();
		} else if (node instanceof Or) {
			return transform(node.getChildren()[0]).orWith(transform(node.getChildren()[1]));
		} else if (node instanceof Equals) {
			return transform(node.getChildren()[0]).biimpWith(transform(node.getChildren()[1]));
		} else {
			throw new UnsupportedOperationException("Operation for node type " + node.getClass() + " not implemented");
		}
	}

	/**
	 * Recursively applies the transformation rules from FM to BDD, starting
	 * from the node {@code feature}.
	 * 
	 * The transformation is [1]:
	 * 
	 * "conjunction of (i) the root feature, (ii) an implication from each child
	 * feature to its parent, (iii) an implication from each feature with a
	 * mandatory child to that child, (iv) an implication from a parent with an
	 * inclusive-OR (exclusive-OR) group to a disjunction (pairwise mutual
	 * exclusion) of the group members.
	 * 
	 * This method does not handle case (i).
	 * 
	 * Mendonca et. al, Efficient Compilation Techniques for Large Scale Feature
	 * Models.
	 * 
	 * @param feature
	 *            the starting point for the transformation.
	 * @return the BDD for the FM starting from {@code feature}, {@code null} if
	 *         {@code parent} has no children
	 */
	private BDD buildBDDFromTree(Feature parent) {
		if (parent.hasChildren()) {
			String parentName = parent.getName();
			// assume nothing about the current Feature parent
			BDD featureBDD = f.one();
			LinkedList<Feature> children = parent.getChildren();			
			
			 /*
			  * case (iv), exclusive-OR
			  * if parent is alternative then the children are alternative among themselves (pairwise).
			  * parent -> (child_1 xor ... xor child_n)
			  */
			if (parent.isAlternative()) {
				/*
				 *  handle for special case: when parent is alternative and has only 1 child, then:
				 *  
				 *  parent -> child
				 */
				if (children.size() == 1) {
					featureBDD.andWith(bdd(parentName).imp(bdd(children.get(0).getName())));
				} else {
					Iterator<Pair<Feature, Feature>> pairsIterator = buildPairs(children);
					
					BDD pairWiseMutualExclusion = null;
					while (pairsIterator.hasNext()) { 
						Pair<Feature,Feature> pair = pairsIterator.next();
						if (pairWiseMutualExclusion == null) {
							pairWiseMutualExclusion = bdd(pair.getO1().getName()).imp(bdd(pair.getO2().getName()).not());
						} else {
							pairWiseMutualExclusion.andWith(bdd(pair.getO1().getName()).imp(bdd(pair.getO2().getName()).not()));
						}
					}
					
					BDD childrenDisjunction = null;
					for (Feature child : children) {
						if (childrenDisjunction == null) {
							childrenDisjunction = bdd(child.getName());
						} else {
							childrenDisjunction.orWith(bdd(child.getName()));
						}
					}
					pairWiseMutualExclusion.andWith(childrenDisjunction);
					
					// careful with the imp/impWith here
					assert (!featureBDD.imp(pairWiseMutualExclusion).isZero());
					featureBDD = bdd(parentName).imp(pairWiseMutualExclusion);
				}
			} else
			/*
			 * case (iv), inclusive-OR
			 * parent -> (child_1 or ... or child_n)
			 */
			if (parent.isOr()) {
				/*
				 *  handle for special case: when parent is alternative and has only 1 child, then:
				 *  
				 *  parent -> child
				 */
				if (children.size() == 1) {
					featureBDD.andWith(bdd(parentName).imp(bdd(children.get(0).getName())));
				} else {
					BDD childrenDisjunction = null;
					for (Feature child : children) {
						if (childrenDisjunction == null) {
							childrenDisjunction = bdd(child.getName());
						} else {
							childrenDisjunction.orWith(bdd(child.getName()));
						}
					}
				
					// same here; careful with the imp/impWith
					assert (!featureBDD.imp(childrenDisjunction).isZero());
					featureBDD = bdd(parentName).imp(childrenDisjunction);
				}
			}
			
			for (Feature child : children) {
				/*
				 * case (ii)
				 * child -> parent relationship
				 */
				assert (!bdd(child.getName()).imp(bdd(parentName)).isZero());
				featureBDD.andWith(bdd(child.getName()).imp(bdd(parentName)));

				/*
				 * case (iii)
				 * if child is mandatory then parent -> child
				 */
				/*
				 * XXX for some reason, a feature can be part of an alternative (or) group, and still be mandatory.
				 * This means that a parent feature p whose children, {a, b}, are alternative can lead to something
				 * like:
				 * 
				 *  ... (p -> (a xor b)) and (p -> a) and (p -> b)...
				 *  
				 *  which boils down to false. Thus the guard !(parent.isAlternative() || parent.isOr()) is needed.
				 */
				if (child.isMandatory() && !(parent.isAlternative() || parent.isOr())) {
					assert (!bdd(parentName).imp(bdd(child.getName())).isZero());
					featureBDD.andWith(bdd(parentName).imp(bdd(child.getName())));
				}
				
				// recurse for every child
				BDD childBDD = buildBDDFromTree(child);
				if (childBDD != null) {
					assert (!childBDD.isZero());
					featureBDD.andWith(childBDD);
				}
			}
			return featureBDD;
		}
		// return null when the feature has no children
		return null;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	private Iterator<Pair<Feature, Feature>> buildPairs(List<Feature> children) {
		Configurations<Feature, ?, Pair<Feature, Feature>>
		combinate = Configurations.combinate(
			new CombinationFactory<Pair<Feature,Feature>, Feature>() {
				@Override
				public Pair<Feature, Feature> makeNew(List<List<Feature>> space, List<Integer> value) {
					return new Pair<Feature,Feature>(space.get(0).get(value.get(0)),space.get(1).get(value.get(1)));
				}
			}, 
			new ArrayList<CombinationFilter<Feature>>() { 
				{
					add(new CombinationFilter<Feature>() {
						@Override
						public boolean accept(List<List<Feature>> space, List<Integer> value) {
							if (space.get(0).get(value.get(0)).equals(space.get(1).get(value.get(1))))
								return false;
							return true;
						}
					}); 
				} 
			},
			children,
			children);
		
		return combinate.iterator();
	}

	/**
	 * Enumerates the {@code featureName} String if it has not been enumerated yet and returns the BDD such that:
	 * 
	 * <b>one</b> <i>and</it> <b>numberOf(</b>{@code featureName}<b>)</b>
	 * 
	 * @param featureName
	 * @return the BDD representing the feature {@code featureName}
	 */
	private BDD bdd(String featureName) {
		int featureNumber = featureNumberer.find(featureName).getNumber();
		BDD featureBDD = f.one().andWith(f.ithVar(featureNumber));
		return featureBDD;
	}

	/**
	 * Singleton instance.
	 */
	private static FMConstraintFactory instance;
	
	/**
	 * Returns an instance of FMConstraintFactory.
	 * @return
	 */
	public static FMConstraintFactory v() {
		if(instance==null) instance = new FMConstraintFactory();		
		return instance;
	}
	
	/**
	 * Defeats instantiation.
	 */
	private FMConstraintFactory() { }
}
