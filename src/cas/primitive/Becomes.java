package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;

/*
 * the become expression type is what's used to create rule mappings, the arrow -> shows how the left argument "becomes"
 * the right argument
 */

public class Becomes extends Expr{

	public Becomes(){}//
	
	public Becomes(Expr left,Expr right) {
		add(left);
		add(right);
	}
	
	public Expr getLeftSide() {
		return get(0);
	}
	public Expr getRightSide() {
		return get(1);
	}
	
	public void setLeftSide(Expr expr) {
		flags.simple = false;
		set(0,expr);
	}
	public void setRightSide(Expr expr) {
		flags.simple = false;
		set(1,expr);
	}
	
	@Override
	public String toString() {
		return getLeftSide()+"->"+getRightSide();
	}
	
	@Override
	public Rule getRule() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	public String typeName() {
		return "becomes";
	}

	@Override
	public String help() {
		return "-> operator\n"
				+ "examples\n"
				+ "f(k)->k^2\n"
				+ "sin(pi)->0";
	}
}
