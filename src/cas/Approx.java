package cas;

public class Approx extends Expr{
	
	private static final long serialVersionUID = 5922084948843351440L;

	Approx(){}//
	public Approx(Expr expr,ExprList defs) {
		add(expr);
		add(defs);
	}

	@Override
	public Expr simplify(Settings settings) {
		return floatExpr(get().convertToFloat((ExprList)get(1)));
	}

	@Override
	public String toString() {
		String out = "";
		out+="approx(";
		out+=get();
		out+=",";
		out+=get(1);
		out+=")";
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat((ExprList)get(1));//kinda pointless but whatever
	}

}
