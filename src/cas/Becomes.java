package cas;

public class Becomes extends Expr{
	private static final long serialVersionUID = 7853485933081337101L;

	Becomes(){}//
	
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
	ExprList getRuleSequence() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

}