package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;

public class Less extends Expr{
	private static final long serialVersionUID = -7710464659230620416L;

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
	public Sequence getRuleSequence() {
		return null;
	}
	
	@Override
	public String typeName() {
		return "less";
	}
}