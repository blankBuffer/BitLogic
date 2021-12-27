package cas;

public class Not extends Expr{
	
	private static final long serialVersionUID = 775872869042676796L;

	Not(){}//
	public Not(Expr e){
		add(e);
	}

	@Override
	public Expr simplify(Settings settings) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		String out = "";
		out+="~";
		out+=get();
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return null;
	}

}
