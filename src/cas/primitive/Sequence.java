package cas.primitive;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

/*
 * this is a list of items where the order MATTERS and CAN have repeats
 */
public class Sequence extends Expr{
	public Sequence() {}//
	
	@Override
	public Rule getRule() {
		return null;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return ComplexFloat.ZERO;
	}
	
	public static Sequence cast(Expr e) {
		if(e == null) return sequence();
		if(e instanceof Sequence) return (Sequence)e;
		if(e instanceof Params || e.typeName().equals("set")) {
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
		out+="[";
		for(int i = 0;i<size();i++) {
			out+=get(i);
			if(i != size()-1) out+=",";
		}
		out+="]";
		return out;
	}
	
	@Override
	public String typeName() {
		return "sequence";
	}

	@Override
	public String help() {
		return "list expression that can contain repeats\n"
				+ "examples\n"
				+ "[2,2,3]\n"
				+ "next([1,2,3,4,5],4)->[6,7,8,9]";
	}
}
