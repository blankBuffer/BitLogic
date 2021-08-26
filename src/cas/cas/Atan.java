package cas;

import java.util.ArrayList;

public class Atan extends Expr{
	
	private static final long serialVersionUID = -8122799157835574716L;

	public Atan(Expr expr) {
		add(expr);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof Atan) if(toBeSimplified.get().negative()) toBeSimplified = prod(num(-1),atan(toBeSimplified.get().abs(settings)).simplify(settings) );
		
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
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return new Atan(get().replace(equs));
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
		return Math.atan(get().convertToFloat(varDefs));
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
