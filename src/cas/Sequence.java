package cas;
/*
 * this is a list of items where the order MATTERS and CAN have repeats
 */
public class Sequence extends Expr{
	private static final long serialVersionUID = 6157362809757172910L;

	@Override
	Sequence getRuleSequence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	
	public static Sequence cast(Expr e) {
		if(e == null) return sequence();
		if(e instanceof Sequence) {
			return (Sequence)e;
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
