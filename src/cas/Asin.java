package cas;

import java.util.ArrayList;

public class Asin extends Expr{
	
	private static final long serialVersionUID = 8245957240404627757L;
	
	static Equ asinSinCase = (Equ)createExpr("asin(sin(x))=x");
	static Equ asinCosCase = (Equ)createExpr("asin(cos(x))=-x+pi/2");

	public Asin(Expr expr) {
		add(expr);
	}
	
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		toBeSimplified.set(0, trigCompress(toBeSimplified.get(),settings) );
		
		if(toBeSimplified instanceof Asin) {
			toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
			if(toBeSimplified.get().negative()) {
				toBeSimplified = neg(asin(toBeSimplified.get().abs(settings))).simplify(settings);
			}
		}
		
		toBeSimplified = toBeSimplified.modifyFromExample(asinSinCase, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(asinCosCase, settings);
		
		if(toBeSimplified instanceof Asin) toBeSimplified = unitCircle((Asin)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}
	
	static ArrayList<Equ> unitCircleTable = new ArrayList<Equ>();
	static void initUnitCircleTable() {
		unitCircleTable.add((Equ)createExpr("asin(0)=0"));
		unitCircleTable.add((Equ)createExpr("asin(1)=pi/2"));
		unitCircleTable.add((Equ)createExpr("asin(sqrt(2)/2)=pi/4"));
		unitCircleTable.add((Equ)createExpr("asin(1/2)=pi/6"));
		unitCircleTable.add((Equ)createExpr("asin(sqrt(3)/2)=pi/3"));
	}
	public Expr unitCircle(Asin asin,Settings settings) {
		if(unitCircleTable.size() == 0) initUnitCircleTable();
		Expr out = asin;
		for(int i = 0;i<unitCircleTable.size();i++) {
			out = asin.modifyFromExample(unitCircleTable.get(i), settings);
			if(!(out instanceof Asin)) break;
		}
		return out;
	}

	@Override
	public Expr copy() {
		Expr out = new Asin(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		return "asin("+get()+")";
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Asin) {
			return other.get().equalStruct(get());
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+serialVersionUID;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.asin(get().convertToFloat(varDefs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Asin) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}

}
