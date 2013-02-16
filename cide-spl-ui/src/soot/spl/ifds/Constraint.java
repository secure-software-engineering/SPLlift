package soot.spl.ifds;

import static soot.spl.ifds.Constraint.FeatureModelMode.NO_SINGLETON;

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import soot.util.NumberedString;
import soot.util.StringNumberer;

public class Constraint<T> implements Cloneable {
	
	public static BDDFactory FACTORY;
	
	public enum FeatureModelMode{
		NONE, 			//do not consider the feature model at all
		ALL,			//consider all feature constraints
		NO_SINGLETON	//consider all feature constraints but singleton constraints of the form "A" or "!A"
	};
	
	public static FeatureModelMode fmMode = NO_SINGLETON;

	@SuppressWarnings({ "rawtypes" })
	private final static Constraint FALSE = new Constraint(null) {
		public Constraint and(Constraint other) {
			//false && other = false
			return this;
		}		

		public Constraint or(Constraint other) {
			//false || other == other
			return other;
		}		

		public String toString() {
			return "false";
		}
		
		public String toString(StringNumberer featureNumberer) {
			return toString();
		}

		public int hashCode() {
			return -436534;
		}
		
		public boolean equals(Object obj) {
			return obj==this;
		}
		
		protected Constraint exists(NumberedString varToQuantify) {
			return this;
		}
		
		public Constraint simplify(Iterable allFeatures, Collection usedFeatures) {
			return this;
		}
		
		public int size() {
			return 0;
		}
	};
	
	@SuppressWarnings({ "rawtypes" })
	private final static Constraint TRUE = new Constraint(null) {
		public Constraint and(Constraint other) {
			//true && other == other
			return other; 
		}
		
		public Constraint or(Constraint other) {
			//true || other == true
			return this;
		}

		public String toString() {
			return "true";
		}
		
		public String toString(StringNumberer featureNumberer) {
			return toString();
		}

		public int hashCode() {
			return -23214;
		}
		
		public boolean equals(Object obj) {
			return obj==this;
		}
		
		protected Constraint exists(NumberedString varToQuantify) {
			return this;
		}
		
		public Constraint simplify(Iterable allFeatures, Collection usedFeatures) {
			return this;
		}

		public int size() {
			return 0;
		}
	};

	protected final BDD bdd;
	
	private Constraint(BitSet elems, boolean positive) {
		synchronized (FACTORY) {
			BDD curr = FACTORY.one();
			if(!elems.isEmpty()) {
				for(int i=elems.nextSetBit(0); i>=0; i=elems.nextSetBit(i+1)) {
					BDD ithVar = FACTORY.ithVar(i);
					curr = curr.andWith(ithVar);
				}
			} else
				curr = curr.not(); //no elements provided; assume the "FALSE" constraint
			if(positive) 
				bdd = curr;
			else
				bdd = curr.not();
		}
	}
	
	private Constraint(BitSet elems, Set<NumberedString> featureDomain) {
		synchronized (FACTORY) {
			BDD curr = FACTORY.one();
			for(NumberedString feature: featureDomain) {
				int i = feature.getNumber();
				BDD ithVar = FACTORY.ithVar(i);
				if(!elems.get(i)) {
					ithVar = ithVar.not();
				}
				curr = curr.andWith(ithVar);
			}
			bdd = curr;
		}
	}
	
	
	/**
	 * Constructs a <i>full</i> constraint in the sense that all variables mentioned in
	 * featureDomain but not mentioned in elems will be automatically negated.
	 * If the domain is {a,b,c} and elems is {b} then this will construct the
	 * constraint !a && b && !c.
	 */
	public static <T> Constraint<T> make(BitSet elems, Set<NumberedString> featureDomain) {
		return new Constraint<T>(elems,featureDomain);
	}

	/**
	 * If positive is true then for elems={a,b} this constructs a constraint
	 * a && b. Otherwise, this constructs a constraint !(a && b).
	 * A constraint of the form a && b does not say anything at all about variables
	 * not mentioned. In particular, a && b is not the same as a && b && !c.
	 */
	public static <T> Constraint<T> make(BitSet elems, boolean positive) {
		if(elems.isEmpty()) throw new RuntimeException("empty constraint!");
		return new Constraint<T>(elems,positive);
	}
	
	public synchronized static <T> Constraint<T> make(BDD bdd) {
		synchronized (FACTORY) {
			if(bdd.isOne())
				return Constraint.trueValue();
			else if(bdd.isZero())
				return Constraint.falseValue();
			else return new Constraint<T>(bdd);
		}
	}
	
	private Constraint(BDD bdd) {
		this.bdd = bdd;		
	}
	
	/**
	 * Computes the constraint representing this OR other.
	 * The constraint is automatically reduced such that
	 * a || !a results in true.
	 * @see Constraint#trueValue()
	 */
	public Constraint<T> or(Constraint<T> other) {
		synchronized (FACTORY) {
			if(other==trueValue()) return other;
			if(other==falseValue()) return this;
			
			BDD disjunction = bdd.or(other.bdd);
			if(disjunction.isOne()) 
				return trueValue();
			else
				return new Constraint<T>(disjunction);
		}
	}
	
	/**
	 * Computes the constraint representing this AND other.
	 * The constraint is automatically reduced such that
	 * a && !a results in false.
	 * @see Constraint#falseValue()
	 */
	public Constraint<T> and(Constraint<T> other) {
		synchronized (FACTORY) {
			if(other==trueValue()) return this;
			if(other==falseValue()) return other;
			
			BDD conjunction = bdd.and(other.bdd);
			if(conjunction.isZero())
				return falseValue();
			else
				return new Constraint<T>(conjunction);
		}
	}
	
	@Override
	public String toString() {
		return bdd.toString();
	}

	public String toString(StringNumberer featureNumberer) {
		synchronized (FACTORY) {
			StringBuilder sb = new StringBuilder();
	        int[] set = new int[FACTORY.varNum()];
			toStringRecurse(FACTORY, sb, bdd, set, featureNumberer);
			return sb.toString();
		}
	}
	
	private static void toStringRecurse(BDDFactory f, StringBuilder sb, BDD r, int[] set,
			StringNumberer featureNumberer) {
		synchronized (FACTORY) {
			int n;
			boolean first;
	
			if (r.isZero())
				return;
			else if (r.isOne()) {
				sb.append("{");
				first = true;
	
				for (n = 0; n < set.length; n++) {
					if (set[n] > 0) {
						if (!first)
							sb.append(" ^ ");
						first = false;
						if (set[n] != 2) {
							sb.append("!");
						}
						sb.append(featureNumberer.get((long) f.level2Var(n)));
					}
				}
				sb.append("} ");
			} else {
				set[f.var2Level(r.var())] = 1;
				BDD rl = r.low();
				toStringRecurse(f, sb, rl, set, featureNumberer);
				rl.free();
	
				set[f.var2Level(r.var())] = 2;
				BDD rh = r.high();
				toStringRecurse(f, sb, rh, set, featureNumberer);
				rh.free();
	
				set[f.var2Level(r.var())] = 0;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Constraint<T> trueValue() {
		return TRUE;
	}

	@SuppressWarnings("unchecked")
	public static <T> Constraint<T> falseValue() {
		return FALSE;
	}

	@Override
	public int hashCode() {
		synchronized (FACTORY) {
			return bdd.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		synchronized (FACTORY) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("rawtypes")
			Constraint other = (Constraint) obj;
			if (bdd == null) {
				if (other.bdd != null)
					return false;
			} else if (!bdd.equals(other.bdd))
				return false;
			return true;
		}
	}

	protected Constraint<T> exists(NumberedString varToQuantify) {
		synchronized (FACTORY) {
			return make(bdd.exist(FACTORY.one().andWith(FACTORY.ithVar(varToQuantify.getNumber()))));
		}
	}
	
	public Constraint<T> simplify(Iterable<NumberedString> allFeatures, Collection<NumberedString> usedFeatures) {
		Constraint<T> fmConstraint = this;
		for (NumberedString feature : allFeatures) {
			if(!usedFeatures.contains(feature)) {
				fmConstraint = fmConstraint.exists(feature);
			}
		}
		return fmConstraint;
	}
	
	public int size() {
		synchronized (FACTORY) {
			return bdd.nodeCount();
		}
	}
}
