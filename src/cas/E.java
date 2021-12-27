package cas;

public class E extends Expr{
	
	private static final long serialVersionUID = -6790558818933715416L;

	public E() {
		flags.simple = true;
		flags.sorted = true;
	}

	@Override
	public Expr simplify(Settings settings) {
		return copy();
	}

	@Override
	public String toString() {
		return "e";
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(Math.E,0);
	}

}
