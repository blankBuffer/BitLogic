package cas.primitive;

import java.io.Serializable;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

import static cas.Cas.*;

public class Var extends Expr{
	
	public static class Assumptions implements Serializable{
		private static final long serialVersionUID = 5994712998417994326L;
		
		private boolean integer = false;
		private boolean complex = true;
		
		public void makeInteger() {
			integer = true;
			complex = false;
		}
		public void makeReal() {
			complex = false;
			integer = false;
		}
		
		public void makeComplex() {
			complex = true;
			integer = false;
		}
		public boolean isComplex() {
			return complex;
		}
		public boolean isReal() {
			return !complex;
		}
		public boolean isInteger() {
			return integer;
		}
		public Assumptions copy() {
			Assumptions out = new Assumptions();
			out.integer = integer;
			out.complex = complex;
			return out;
		}
	}
	
	public String name;
	public Assumptions assumptions = null;
	
	public static final Var PI = pi();
	public static final Var E = e();
	public static final Var INF = inf();
	public static final Expr NEG_INF = neg(inf());
	public static final Var EPSILON = epsilon();
	public static final Expr NEG_EPSILON = neg(epsilon());
	public static final Var ERROR = error();
	public static final Expr SUCCESS = var("SUCCESS");
	
	public static void init() {
		NEG_INF.setMutableFullTree(false);
		NEG_EPSILON.setMutableFullTree(false);
	}
	
	/*
	 * for non generic variables this is the defined value for the constant
	 */
	public ComplexFloat valuef = new ComplexFloat(0,0);
	private void specialVars(String name) {
		if(name.equals("pi")){
			generic = false;
			valuef.real = Math.PI;
		}else if(name.equals("e")){
			generic = false;
			valuef.real = Math.E;
		}else if(name.equals("inf")){
			generic = false;
			valuef.real = Double.POSITIVE_INFINITY;
		}else if(name.equals("epsilon")){
			generic = false;
			valuef.real = 0.0000000001;
		}
	}
	
	public Var(String name,Assumptions assumptions){
		specialVars(name);
		this.name = name;
		this.assumptions = assumptions;
		
		setSortedSingleNode(true);
	}
	public Var(String name) {
		specialVars(name);
		this.name = name;
		this.assumptions = new Assumptions();
		
		setSortedSingleNode(true);
	}

	
	/*
	 * non generic variables behave like numbers and are constant
	 * many functions handle variables differently if they are generic
	 */
	private boolean generic = true;
	public boolean isGeneric() {
		return generic;
	}
	@Override
	public Expr copy() {
		Var out = new Var(name);
		out.assumptions = assumptions.copy();
		return out;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof Var) {
			return ((Var)other).name.equals(name);//check if strings are equal
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode()+980698164;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		for(int i = 0;i<varDefs.size();i++) {
			Func tempEqu = (Func)varDefs.get(i);
			Var otherVar = (Var)Equ.getLeftSide(tempEqu);
			if(equals(otherVar)) {
				if(Equ.getRightSide(tempEqu) instanceof FloatExpr) {
					return ((FloatExpr)Equ.getRightSide(tempEqu)).value;
				}
				return Equ.getRightSide(tempEqu).convertToFloat(exprSet());
			}
		}
		return valuef;
	}
	@Override
	public Rule getRule() {
		return null;
	}

	@Override
	public String typeName() {
		return "var";
	}

	@Override
	public String help() {
		return "variable expression\n"
				+ "examples\n"
				+ "x\n"
				+ "x+x+y->2*x+y";
	}
}
