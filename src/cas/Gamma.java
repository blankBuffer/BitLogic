package cas;

public class Gamma extends Expr{
	
	private static final long serialVersionUID = 8145392107245407249L;

	public Gamma(Expr e) {
		add(e);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}

	@Override
	public Expr copy() {
		return new Gamma(get().copy());
	}

	@Override
	public String toString() {
		String out = "";
		out+="gamma(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Gamma) {
			return other.get().equalStruct(get());
		}
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
		Gamma repl = gamma(get().replace(equs));
		return repl;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat inner = ComplexFloat.sub(get().convertToFloat(varDefs),new ComplexFloat(1.0,0));
		
		return new ComplexFloat(factorial(inner.real),0);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		// TODO Auto-generated method stub
		return false;
	}

}
