package cas.programming;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

public class Script extends Expr{

	int addedVariables = 0;
	
	public Script(){}//
	
	@Override
	public Expr copy() {
		Script out = new Script();
		
		for(int i = 0;i<size();i++) {
			out.add(get(i).copy());
		}
		out.addedVariables = addedVariables;
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		for(int i = 0;i<size();i++) {
			out+=get(i)+";";
		}
		return out;
	}
	
	public static Script cast(Expr e) {
		if(e instanceof Script) {
			return (Script)e;
		}
		
		Script s = new Script();
		s.add(e);
		return s;
		
	}
	
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		// TODO Auto-generated method stub
		return ComplexFloat.ZERO;
	}

	@Override
	public Rule getRule() {
		return null;
	}
	
	@Override
	public String typeName() {
		return "script";
	}

	@Override
	public String help() {
		// TODO Auto-generated method stub
		return null;
	}
}
