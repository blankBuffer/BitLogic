package cas.primitive;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

public class Greater extends Expr{
	public Greater(){}//
	public Greater(Expr leftSide,Expr rightSide){
		add(leftSide);
		add(rightSide);
	}
	public Expr getLeftSide() {
		return get(0);
	}
	public Expr getRightSide() {
		return get(1);
	}
	
	public void setLeftSide(Expr expr) {
		set(0,expr);
	}
	public void setRightSide(Expr expr) {
		set(1,expr);
	}

	@Override
	public String toString() {
		String out = "";
		out+=getLeftSide().toString();
		out+='>';
		out+=getRightSide().toString();
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return getRightSide().convertToFloat(varDefs);//usually the solution is on the right side of the equation
	}
	@Override
	public Rule getRule() {
		return null;
	}
	
	@Override
	public String typeName() {
		return "greater";
	}
	@Override
	public String help() {
		return "> operator\n"
				+ "examples\n"
				+ "eval(3>2)->true\n"
				+ "eval(2*x>2*x)->false";
	}
}
