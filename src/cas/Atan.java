package cas;

public class Atan extends Expr{
	
	private static final long serialVersionUID = -8122799157835574716L;
	
	static Equ containsInverse = (Equ)createExpr("atan(tan(x))=x");

	public Atan(Expr expr) {
		add(expr);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof Atan) {
			toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
			if(toBeSimplified.get().negative()) {
				toBeSimplified = prod(num(-1),atan(toBeSimplified.get().abs(settings)).simplify(settings) );
			}
		}
		if(toBeSimplified instanceof Atan) toBeSimplified.set(0,distr(toBeSimplified.get()).simplify(settings));
		
		if(toBeSimplified instanceof Atan) toBeSimplified = toBeSimplified.modifyFromExample(containsInverse, settings);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}

	@Override
	public Expr copy() {
		Atan out = new Atan(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="atan(";
		out+=get();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Atan) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+9163701745123051623L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return new Atan(get().replace(equs));
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.atan(get().convertToFloat(varDefs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Atan) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}

}
