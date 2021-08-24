package cas;
import java.math.BigInteger;
import java.util.ArrayList;

public class Num extends Expr{
	
	private static final long serialVersionUID = -8648260475281043831L;
	public BigInteger value;
	
	private void setFlags() {
		flags.simple = true;
		flags.sorted = true;
	}
	
	public Num(long num) {
		value = BigInteger.valueOf(num);
		setFlags();
	}
	public Num(String num) {
		value = new BigInteger(num);
		setFlags();
	}
	public Num(BigInteger num) {
		value = num;
		setFlags();
	}

	@Override
	public Expr simplify(Settings settings) {//nothing to be simplified
		return copy();
	}

	@Override
	public String toString() {
		return value.toString();
	}
	@Override
	public Expr copy() {
		return new Num(value);
	}
	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Num) {
			Num otherCasted = (Num)other;
			return otherCasted.value.equals(value);
		}
		return false;
	}
	@Override
	boolean similarStruct(Expr other,boolean checked) {
		if(other instanceof Num) return equalStruct((Num)other);
		else return false;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return copy();
	}
	@Override
	public long generateHash() {
		return value.longValue()+2762923428917024652L;
	}
	@Override
	public double convertToFloat(ExprList varDefs) {
		return value.doubleValue();
	}

}
