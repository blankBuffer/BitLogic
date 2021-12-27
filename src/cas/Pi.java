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
	public String toString() {
		return "pi";
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(Math.PI,0);
	}

}
