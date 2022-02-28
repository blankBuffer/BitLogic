package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;

/*
 * this is a list of items where the order MATTERS and CAN have repeats
 */
public class Sequence extends Expr{
	private static final long serialVersionUID = 6157362809757172910L;

	public Sequence() {}//
	
	@Override
	public Sequence getRuleSequence() {
		return null;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	
	public static Sequence cast(Expr e) {
		if(e == null) return sequence();
		if(e instanceof Sequence) return (Sequence)e;
		if(e instanceof Params) {
			Sequence out = new Sequence();
			for(int i = 0;i<e.size();i++) {
				out.add(e.get(i));
			}
			return out;
		}
		return sequence(e);
	}
	
	@Override
	public String toString() {
		String out = "";
		out+="{";
		for(int i = 0;i<size();i++) {
			out+=get(i);
			if(i != size()-1) out+=",";
		}
		out+="}";
		return out;
	}
}
