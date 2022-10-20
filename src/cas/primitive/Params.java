package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;

public class Params extends Expr{
	public Params(){}//
	public Params(Expr e){
		add(e);
	}

	@Override
	public Rule getRule() {
		return null;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	
	public static Params cast(Expr e) {
		if(e == null) return new Params();
		else if(e instanceof Params) return (Params)e;
		return new Params(e);
	}
	
	@Override
	public String toString() {
		String out = "";
		for(int i = 0;i<size();i++) {
			out+=get(i);
			if(i != size()-1) out+=",";
		}
		return out;
	}

	@Override
	public String typeName() {
		return "params";
	}
	@Override
	public String help() {
		return "params expression holds parameters for functions\n"
				+ "examples\n"
				+ "choose(5,2)->10\n"
				+ "2,3";
	}
}
