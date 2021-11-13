package cas;

public class Var extends Expr{
	
	private static final long serialVersionUID = -3581525014075161068L;
	public String name;
	
	public Var(String name){
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
		return new Var(name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Var) {
			return ((Var)other).name.equals(name);//check if strings are equal
		}
		return false;
	}

	@Override
	boolean similarStruct(Expr other,boolean checked) {
		return true;
	}

	@Override
	public long generateHash() {
		long ex = 1;
		long sum = 0;
		for(int i = 0;i<name.length();i++) {
			sum+= (name.charAt(i)-'0')*ex;
			ex*=63;//think of letters as numbers, i'm aware that it is not perfect but does the job
		}
		return sum;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		for(int i = 0;i<varDefs.size();i++) {
			Equ temp = (Equ)varDefs.get(i);
			Var otherVar = (Var)temp.getLeftSide();
			if(equalStruct(otherVar)) {
				if(temp.getRightSide() instanceof FloatExpr) {
					return ((FloatExpr)temp.getRightSide()).value;
				}
				return temp.getRightSide().convertToFloat(new ExprList());
			}
		}
		return new ComplexFloat(0,0);
	}

}
