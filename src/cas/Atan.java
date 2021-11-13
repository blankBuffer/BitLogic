package cas;

import java.util.ArrayList;

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
		
		toBeSimplified.set(0, trigCompress(toBeSimplified.get(),settings) );
		
		if(toBeSimplified instanceof Atan) {
			toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
			if(toBeSimplified.get().negative()) {
				toBeSimplified =neg(atan(toBeSimplified.get().abs(settings))).simplify(settings);
			}
		}
		if(toBeSimplified instanceof Atan) toBeSimplified.set(0,distr(toBeSimplified.get()).simplify(settings));
		
		if(toBeSimplified instanceof Atan) toBeSimplified = toBeSimplified.modifyFromExample(containsInverse, settings);
		
		if(toBeSimplified instanceof Atan) toBeSimplified = unitCircle((Atan)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	
	static ArrayList<Equ> unitCircleTable = new ArrayList<Equ>();
	static void initUnitCircleTable() {
		unitCircleTable.add((Equ)createExpr("atan(0)=0"));
		unitCircleTable.add((Equ)createExpr("atan(1)=pi/4"));
		unitCircleTable.add((Equ)createExpr("atan(sqrt(3))=pi/3"));
		unitCircleTable.add((Equ)createExpr("atan(sqrt(3)/3)=pi/6"));
	}
	public Expr unitCircle(Atan atan,Settings settings) {
		if(unitCircleTable.size() == 0) initUnitCircleTable();
		Expr out = atan;
		for(int i = 0;i<unitCircleTable.size();i++) {
			out = atan.modifyFromExample(unitCircleTable.get(i), settings);
			if(!(out instanceof Atan)) break;
		}
		return out;
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
