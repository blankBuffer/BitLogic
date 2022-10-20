package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;

public class Less extends Expr{
	public Less(){}//
	public Less(Expr leftSide,Expr rightSide){
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
		out+='<';
		out+=getRightSide().toString();
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return getRightSide().convertToFloat(varDefs);//usually the solution is on the right side of the equation
	}
	@Override
	public Rule getRule() {
		return null;
	}
	
	@Override
	public String typeName() {
		return "less";
	}
	@Override
	public String help() {
		return "< operator\n"
				+ "examples\n"
				+ "eval(2<3)->true\n"
				+ "eval(2*x<2*x)->false";
	}
}
