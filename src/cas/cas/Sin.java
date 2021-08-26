package cas;

import java.util.ArrayList;

public class Sin extends Expr{
	
	private static final long serialVersionUID = -5759564792496416862L;

	public Sin(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof Sin) if(toBeSimplified.get().negative()) toBeSimplified = prod(num(-1),sin(toBeSimplified.get().abs(settings)).simplify(settings) );
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}

	@Override
	public Expr copy() {
		Sin out = new Sin(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="sin(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Sin) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+9127304624184602649L;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return new Sin(get().replace(equs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Sin) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
		return Math.sin(get().convertToFloat(varDefs));
	}

}
