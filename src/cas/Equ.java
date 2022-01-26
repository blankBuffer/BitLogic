package cas;

public class Equ extends Expr{
	
	private static final long serialVersionUID = 4002612647044850391L;

	Equ(){}//
	public Equ(Expr leftSide,Expr rightSide){
		add(leftSide);
		add(rightSide);
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
		String out = "";
		out+=getLeftSide().toString();
		out+='=';
		out+=getRightSide().toString();
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	@Override
	ExprList getRuleSequence() {
		return null;
	}

}
