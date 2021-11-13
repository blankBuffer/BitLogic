package cas;

public class Pi extends Expr{

	private static final long serialVersionUID = 6874436127983053553L;

	public Pi() {
		flags.simple = true;
		flags.sorted = true;
	}

	@Override
	public Expr simplify(Settings settings) {
		return copy();
	}

	@Override
	public Expr copy() {
		return new Pi();
	}

	@Override
	public String toString() {
		return "pi";
	}

	@Override
	public boolean equalStruct(Expr other) {
		return other instanceof Pi;
	}

	@Override
	boolean similarStruct(Expr other,boolean checked) {
		return other instanceof Pi;
	}
	
	@Override
	public long generateHash() {
		return 1985401253308462194L;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(Math.PI,0);
	}

}
