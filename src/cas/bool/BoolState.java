package cas.bool;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.primitive.ExprList;

public class BoolState extends Expr{

	public static final Expr FALSE = bool(false),TRUE = bool(true);
	public boolean state = false;
	
	public BoolState(boolean state){
		this.state = state;
		flags.simple = true;
		flags.sorted = true;
	}
	
	@Override
	public Expr simplify(CasInfo casInfo) {
		return copy();
	}

	@Override
	public Expr copy() {
		return new BoolState(state);
	}

	@Override
	public String toString() {
		return Boolean.toString(state);
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof BoolState) {
			BoolState otherCasted = (BoolState)other;
			if(otherCasted.state == state) return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		if(state) return -926360784;
		return 971308753;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		if(state == true) return new ComplexFloat(1,0);
		return new ComplexFloat(0,0);
	}

	@Override
	public Rule getRule() {
		return null;
	}
	
	@Override
	public String typeName() {
		return "boolState";
	}

	@Override
	public String help() {
		return "bool expression\n"
				+ "examples\n"
				+ "true|false->true\n"
				+ "contains(x^2,y)->false";
	}

}
