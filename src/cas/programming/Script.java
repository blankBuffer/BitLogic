package cas.programming;

import cas.ComplexFloat;
import cas.Expr;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Script extends Expr{

	private static final long serialVersionUID = -3385077575450663182L;
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
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return new ComplexFloat(0,0);
	}

	@Override
	public Sequence getRuleSequence() {
		return null;
	}
}
