package cas;

public class Equ extends Expr{
	
	private static final long serialVersionUID = 4002612647044850391L;

	public Equ(Expr leftSide,Expr rightSide){
		add(leftSide);
		add(rightSide);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
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
	public Expr copy() {
		Equ out = new Equ(getLeftSide(),getRightSide());
		out.flags.set(flags);
		return out;
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
	public boolean equalStruct(Expr other) {
		if(other instanceof Equ) {
			Equ otherCasted = (Equ)other;
			boolean case1 = getLeftSide().equalStruct(otherCasted.getLeftSide()) && getRightSide().equalStruct(otherCasted.getRightSide());
			boolean case2 = getRightSide().equalStruct(otherCasted.getLeftSide()) && getLeftSide().equalStruct(otherCasted.getRightSide());
			return case1 || case2;
		}
		return false;
	}

	@Override
	boolean similarStruct(Expr other,boolean checked) {
		if(other instanceof Equ) {
			
			if(!checked) if(checkForMatches(other) == false) return false;
			
			Equ otherCasted = (Equ)other;
			
			boolean similarLeftSide = false,similarRightSide = false;
			
			if(getLeftSide().fastSimilarStruct(otherCasted.getLeftSide())) similarLeftSide = true; 
			if(getRightSide().fastSimilarStruct(otherCasted.getRightSide())) similarRightSide = true; 
			
			if(similarLeftSide && similarRightSide) return true;
			
			
		}
		return false;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		Expr left = getLeftSide().replace(equs);
		Expr right = getRightSide().replace(equs);
		return new Equ(left,right);
	}

	@Override
	public long generateHash() {
		return (getLeftSide().generateHash()+getRightSide().generateHash())+9142388263983737432L;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

}
