package cas;

public class If extends Expr{
	
	private static final long serialVersionUID = -7181628821034162659L;

	If(){}//
	public If(Equ eq,Script s) {
		add(eq);
		add(s);
	}

	@Override
	public Expr simplify(Settings settings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return null;
	}

}
