package cas;

import java.util.ArrayList;

public class Cos extends Expr{
	
	private static final long serialVersionUID = -529344373251624547L;

	public Cos(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0, toBeSimplified.get().abs(settings));
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}

	@Override
	public Expr copy() {
		Cos out = new Cos(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="cos(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Cos) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+8236910273651944021L;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return new Cos(get().replace(equs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Cos) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
	@Override
	public double convertToFloat(ExprList varDefs) {
		return Math.cos(get().convertToFloat(varDefs));
	}
}
