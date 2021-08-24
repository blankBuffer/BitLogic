package cas;

import java.util.ArrayList;

public class FloatExpr extends Expr{
	
	private static final long serialVersionUID = 1919695097463437480L;
	
	public double value = 0;
	
	private void setFlags() {
		flags.simple = true;
		flags.sorted = true;
	}
	
	public FloatExpr(double value) {
		this.value = value;
		setFlags();
	}
	
	public FloatExpr(String str) {
		value = Double.parseDouble(str);
		setFlags();
	}

	@Override
	public Expr simplify(Settings settings) {
		return copy();
	}

	@Override
	public Expr copy() {
		return new FloatExpr(value);
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof FloatExpr) {
			FloatExpr otherCasted = (FloatExpr)other;
			return otherCasted.value == value;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return Double.doubleToLongBits(value);//lol
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return copy();
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
		return value;
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof FloatExpr) return equalStruct((Num)other);
		else return false;
	}

}
