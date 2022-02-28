package cas.programming;

import cas.ComplexFloat;
import cas.Expr;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Script extends Expr{

	private static final long serialVersionUID = -3385077575450663182L;
	
	public Script(){}//

	@Override
	public String toString() {
		String out = "";
		if(size() == 0) return "{emptyScript;}";
		out+='{';
		for(int i = 0;i<size();i++) {
			out+=get(i)+";";
		}
		out+='}';
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
		return null;
	}

	@Override
	public Sequence getRuleSequence() {
		return null;
	}
}
