package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;

public class Params extends Expr{
	private static final long serialVersionUID = 1899960538100104923L;
	
	public Params(){}//
	public Params(Expr e){
		add(e);
	}

	@Override
	public Sequence getRuleSequence() {
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

}
