package cas.bool;

import cas.ComplexFloat;
import cas.Expr;
import cas.Settings;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class BoolState extends Expr{

	private static final long serialVersionUID = 4155968576732862760L;
	public static final Expr FALSE = bool(false),TRUE = bool(true);
	boolean state = false;
	
	public BoolState(boolean state){
		this.state = state;
		flags.simple = true;
		flags.sorted = true;
	}
	
	@Override
	public Expr simplify(Settings settings) {
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
	public Sequence getRuleSequence() {
		return null;
	}

}
