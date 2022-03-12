package cas.calculus;

import cas.CasInfo;
import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.primitive.*;

public class Range extends Expr{
	private static final long serialVersionUID = -6321836409665890872L;
	
	public Range() {}//
	
	public Range(Expr domainMin,Expr domainMax,Expr e,Var v) {
		add(domainMin);
		add(domainMax);
		add(e);
		add(v);
	}
	
	public Expr getMin() {
		return get(0);
	}
	
	public Expr getMax() {
		return get(1);
	}
	
	@Override
	public Var getVar() {
		return (Var)get(3);
	}
	
	public Expr getExpr() {
		return get(2);
	}

	static Rule calculateRange = new Rule("calculate the range from critical points") {
		private static final long serialVersionUID = 1L;
		
		ExprList getCriticalPoints(Expr e,Var v,CasInfo casInfo) {
			Expr derivative = diff(e,v).simplify(casInfo);
			ExprList solutions = ExprList.cast(solve(equ(derivative,num(0)),v).simplify(casInfo));
			return solutions;
		}
		
		Expr maximum(Expr e,ExprList criticalPoints,CasInfo casInfo) {
			Expr currentMax = e.replace( (Equ)criticalPoints.get(0) ).simplify(casInfo);
			System.out.println(currentMax);
			ComplexFloat floatMax = currentMax.convertToFloat(exprList());
			
			for(int i = 1;i<criticalPoints.size();i++) {
				Expr current = e.replace( (Equ)criticalPoints.get(i)).simplify(casInfo);
				ComplexFloat currentApprox = current.convertToFloat(exprList());
				if(currentApprox.real > floatMax.real) {
					floatMax = currentApprox;
					currentMax = current;
				}
			}
			return currentMax;
		}
		Expr minimum(Expr e,ExprList criticalPoints,CasInfo casInfo) {
			Expr currentMax = e.replace( (Equ)criticalPoints.get(0) ).simplify(casInfo);
			System.out.println(currentMax);
			ComplexFloat floatMax = currentMax.convertToFloat(exprList());
			
			for(int i = 1;i<criticalPoints.size();i++) {
				Expr current = e.replace( (Equ)criticalPoints.get(i)).simplify(casInfo);
				ComplexFloat currentApprox = current.convertToFloat(exprList());
				if(currentApprox.real < floatMax.real) {
					floatMax = currentApprox;
					currentMax = current;
				}
			}
			return currentMax;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Range range = (Range)e;
			
			
			ExprList criticalPoints = getCriticalPoints(range.getExpr(),range.getVar(),casInfo);
			
			
			{//remove criticalPoints outside domain
				ComplexFloat minApprox = range.getMin().convertToFloat(exprList());
				ComplexFloat maxApprox = range.getMax().convertToFloat(exprList());
				
				for(int i = 0;i<criticalPoints.size();i++) {
					Expr critVal = ((Equ)criticalPoints.get(i)).getRightSide();
					ComplexFloat approx = critVal.convertToFloat(exprList());
					
					if(approx.real > maxApprox.real || approx.real < minApprox.real) {
						criticalPoints.remove(i);
						i--;
					}
					
				}
			}
			
			criticalPoints.add(equ(range.getVar(),range.getMin()));
			criticalPoints.add(equ(range.getVar(),range.getMax()));
			
			Expr min = minimum(range.getExpr(), criticalPoints,casInfo);
			Expr max = maximum(range.getExpr(), criticalPoints,casInfo);
			
			return sequence(min,max);
		}
	};
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(
				calculateRange
		);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	public String typeName() {
		return "range";
	}
	
}
