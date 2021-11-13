package cas;

public class If extends Expr{
	
	private static final long serialVersionUID = -7181628821034162659L;

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
	public Expr copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
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
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		// TODO Auto-generated method stub
		return false;
	}

}
