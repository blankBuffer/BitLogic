package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;

/*
 * the become expression type is what's used to create rule mappings, the arrow -> shows how the left argument "becomes"
 * the right argument
 */

public class Becomes extends Expr{
	private static final long serialVersionUID = 7853485933081337101L;

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
	
	void setLeftSide(Expr expr) {
		flags.simple = false;
		set(0,expr);
	}
	void setRightSide(Expr expr) {
		flags.simple = false;
		set(1,expr);
	}
	
	@Override
	public String toString() {
		return getLeftSide()+"->"+getRightSide();
	}
	
	@Override
	public Sequence getRuleSequence() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

}
