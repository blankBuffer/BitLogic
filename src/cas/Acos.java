package cas;

import java.util.ArrayList;

public class Acos extends Expr{
	
	private static final long serialVersionUID = 3855238699397076495L;
	static Equ acosCosCase = (Equ)createExpr("acos(cos(x))=x");
	static Equ acosSinCase = (Equ)createExpr("acos(sin(x))=-x+pi/2");

	public Acos(Expr expr) {
		add(expr);
	}
	
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		toBeSimplified.set(0, trigCompress(toBeSimplified.get(),settings) );
		
		if(toBeSimplified instanceof Acos) {
			toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
			if(toBeSimplified.get().negative()) {
				toBeSimplified = sum(neg(acos(toBeSimplified.get().abs(settings))),pi()).simplify(settings);
			}
		}
		
		toBeSimplified = toBeSimplified.modifyFromExample(acosCosCase, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(acosSinCase, settings);
		
		if(toBeSimplified instanceof Acos) toBeSimplified = unitCircle((Acos)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}
	
	static ArrayList<Equ> unitCircleTable = new ArrayList<Equ>();
	static void initUnitCircleTable() {
		unitCircleTable.add((Equ)createExpr("acos(0)=pi/2"));
		unitCircleTable.add((Equ)createExpr("acos(1)=0"));
		unitCircleTable.add((Equ)createExpr("acos(sqrt(2)/2)=pi/4"));
		unitCircleTable.add((Equ)createExpr("acos(1/2)=pi/3"));
		unitCircleTable.add((Equ)createExpr("acos(sqrt(3)/2)=pi/6"));
	}
	public Expr unitCircle(Acos acos,Settings settings) {
		if(unitCircleTable.size() == 0) initUnitCircleTable();
		Expr out = acos;
		for(int i = 0;i<unitCircleTable.size();i++) {
			out = acos.modifyFromExample(unitCircleTable.get(i), settings);
			if(!(out instanceof Acos)) break;
		}
		return out;
	}

	@Override
	public Expr copy() {
		Expr out = new Acos(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		return "acos("+get()+")";
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Acos) {
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
		return ComplexFloat.acos(get().convertToFloat(varDefs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Acos) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
}
