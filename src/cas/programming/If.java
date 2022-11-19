package cas.programming;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

public class If extends Expr{
	
	If(){}//
	public If(Func equ,Script s) {
		add(equ);
		add(s);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Rule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String typeName() {
		return "if";
	}
	@Override
	public String help() {
		// TODO Auto-generated method stub
		return null;
	}
}
