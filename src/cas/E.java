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
	public Expr copy() {
		return new E();
	}

	@Override
	public String toString() {
		return "e";
	}

	@Override
	public boolean equalStruct(Expr other) {
		return other instanceof E;
	}

	@Override
	boolean similarStruct(Expr other,boolean cheched) {
		return other instanceof E;
	}

	@Override
	public long generateHash() {
		return 2387620836382320210L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return copy();
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(Math.E,0);
	}

}
