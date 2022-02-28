package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Settings;

public class Var extends Expr{
	
	private static final long serialVersionUID = -3581525014075161068L;
	public String name;
	public ComplexFloat valuef = new ComplexFloat(0,0);
	
	public boolean generic = true;//if it is easily similar
	
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
		}
		this.name = name;
		flags.simple = true;//variable is as simple as it gets
		flags.sorted = true;
	}
	
	@Override
	public Expr simplify(Settings settings) {//nothing to simplify
		return copy();
	}

	@Override
	public Expr copy() {
		Var out = new Var(name);
		out.valuef = valuef;
		out.generic = generic;
		flags.simple = true;
		flags.sorted = true;
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
		int ex = 913478934;
		int sum = 0;
		for(int i = 0;i<name.length();i++) {
			sum+= (name.charAt(i))*ex;
			ex*=1508572583;
		}
		return sum+891267084;
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

}
