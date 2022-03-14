package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;

public class Var extends Expr{
	
	private static final long serialVersionUID = -3581525014075161068L;
	public String name;
	
	public static final Var PI = pi();
	public static final Var E = e();
	public static final Var INF = inf();
	public static final Expr NEG_INF = neg(inf());
	public static final Var EPSILON = epsilon();
	public static final Expr NEG_EPSILON = neg(epsilon());
	
	/*
	 * for non generic variables this is the defined value for the constant
	 */
	public ComplexFloat valuef = new ComplexFloat(0,0);
	
	public Var(String name){
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
		this.name = name;
		flags.sorted = true;
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
		return new Var(name);
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
	public ComplexFloat convertToFloat(ExprList varDefs) {
		for(int i = 0;i<varDefs.size();i++) {
			Equ temp = (Equ)varDefs.get(i);
			Var otherVar = (Var)temp.getLeftSide();
			if(equals(otherVar)) {
				if(temp.getRightSide() instanceof FloatExpr) {
					return ((FloatExpr)temp.getRightSide()).value;
				}
				return temp.getRightSide().convertToFloat(new ExprList());
			}
		}
		return valuef;
	}
	@Override
	public Sequence getRuleSequence() {
		return null;
	}

	@Override
	public String typeName() {
		return "var";
	}
}
