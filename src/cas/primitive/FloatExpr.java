package cas.primitive;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

public class FloatExpr extends Expr{
	
	public ComplexFloat value = new ComplexFloat(0,0);
	
	private void setFlags() {
		setSimpleSingleNode(true);
		setSortedSingleNode(true);
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
	public Expr simplify(CasInfo casInfo) {
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
	public ComplexFloat convertToFloat(Func varDefs) {
		return value;
	}

	@Override
	public Rule getRule() {
		return null;
	}

	@Override
	public String typeName() {
		return "floatExpr";
	}

	@Override
	public String help() {
		return "floatExpr expression\n"
				+ "examples\n"
				+ "approx(2.2+5.3)->7.5\n"
				+ "7.123";
	}
}
