package cas;

public class Equ extends Expr{
	
	private static final long serialVersionUID = 4002612647044850391L;
	static final int EQUALS = 0,GREATER = 1,LESS = -1;
	int type = EQUALS;

	Equ(){}//
	public Equ(Expr leftSide,Expr rightSide,int type){
		add(leftSide);
		add(rightSide);
		this.type = type;
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
		if(type == EQUALS) out+='=';
		else if(type == GREATER) out+='>';
		else if(type == LESS) out+='<';
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
	
	@Override
	public Expr copy() {
		Equ out = new Equ(getLeftSide().copy(),getRightSide().copy(),type);
		
		out.flags.set(flags);
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o instanceof Equ) {
			Equ casted = (Equ)o;
			return casted.type == type && casted.getLeftSide().equals(getLeftSide()) && casted.getRightSide().equals(getRightSide());
		}
		return false;
	}
}
