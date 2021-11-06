package cas;

public class Approx extends Expr{
	
	private static final long serialVersionUID = 5922084948843351440L;

	public Approx(Expr expr,ExprList defs) {
		add(expr);
		add(defs);
	}

	@Override
	public Expr simplify(Settings settings) {
		return floatExpr(get().convertToFloat((ExprList)get(1)));
	}

	@Override
	public Expr copy() {
		return new Approx(get().copy(),(ExprList)get(1).copy());
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
	public boolean equalStruct(Expr other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long generateHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		
		
		
		return new Approx(get().replace(equs),(ExprList) get(1).copy());
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat((ExprList)get(1));//kinda pointless but whatever
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		// TODO Auto-generated method stub
		return false;
	}

}
