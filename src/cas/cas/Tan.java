package cas;

import java.util.ArrayList;

public class Tan extends Expr{
	
	private static final long serialVersionUID = -2282985074053649819L;

	public Tan(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof Tan) if(toBeSimplified.get().negative()) toBeSimplified = prod(num(-1),tan(toBeSimplified.get().abs(settings)).simplify(settings) );
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}

	@Override
	public Expr copy() {
		Tan out = new Tan(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="tan(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Tan) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+927142837462378103L;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return new Tan(get().replace(equs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Tan) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
	@Override
	public double convertToFloat(ExprList varDefs) {
		return Math.tan(get().convertToFloat(varDefs));
	}

}
