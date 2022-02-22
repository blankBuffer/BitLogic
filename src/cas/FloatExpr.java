package cas;

public class FloatExpr extends Expr{
	
	private static final long serialVersionUID = 1919695097463437480L;
	
	public ComplexFloat value = new ComplexFloat(0,0);
	
	private void setFlags() {
		flags.simple = true;
		flags.sorted = true;
	}
	
	public FloatExpr(double value) {
		this.value.real = value;
		setFlags();
	}
	public FloatExpr(ComplexFloat complexFloat) {
		value.set(complexFloat);
		setFlags();
	}
	
	public FloatExpr(String str) {
		value.real = Double.parseDouble(str);
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
		return value.toString();
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof FloatExpr) {
			FloatExpr otherCasted = (FloatExpr)other;
			return otherCasted.value == value;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(value.real)+120987234*Double.hashCode(value.imag);
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return value;
	}

	@Override
	Sequence getRuleSequence() {
		return null;
	}

}
