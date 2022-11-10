package cas.calculus;

import cas.CasInfo;
import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.primitive.*;

public class Range extends Expr{
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
		Func getCriticalPoints(Expr e,Var v,CasInfo casInfo) {//returns set
			Expr derivative = diff(e,v).simplify(casInfo);
			Func derivativeSolutionsSet = ExprSet.cast(solve(equ(derivative,num(0)),v).simplify(casInfo));
			Func solutionsSet = ExprSet.cast(solve(equ(e,num(0)),v).simplify(casInfo));
			
			Func criticalPointsSet = exprSet();
			
			for(int i = 0;i<solutionsSet.size();i++) {
				if(!solutionsSet.get(i).containsType("solve")) criticalPointsSet.add(solutionsSet.get(i));
			}
			for(int i = 0;i<derivativeSolutionsSet.size();i++) {
				if(!derivativeSolutionsSet.get(i).containsType("solve")) criticalPointsSet.add(derivativeSolutionsSet.get(i));
			}
			
			return ExprSet.cast(criticalPointsSet.simplify(casInfo));
		}
		
		Expr maximum(Expr e,Func criticalPointsSet,CasInfo casInfo) {
			Expr currentMax = e.replace( (Func)criticalPointsSet.get(0) ).simplify(casInfo);
			ComplexFloat floatMax = currentMax.convertToFloat(exprSet());
			
			for(int i = 1;i<criticalPointsSet.size();i++) {
				Expr current = e.replace( (Func)criticalPointsSet.get(i)).simplify(casInfo);
				ComplexFloat currentApprox = current.convertToFloat(exprSet());
				
				if(currentApprox.real > floatMax.real && currentApprox.real()) {
					floatMax = currentApprox;
					currentMax = current;
				}
			}
			return currentMax;
		}
		Expr minimum(Expr e,Func criticalPointsSet,CasInfo casInfo) {
			Expr currentMax = e.replace( (Func)criticalPointsSet.get(0) ).simplify(casInfo);
			ComplexFloat floatMax = currentMax.convertToFloat(exprSet());
			
			for(int i = 1;i<criticalPointsSet.size();i++) {
				Expr current = e.replace( (Func)criticalPointsSet.get(i)).simplify(casInfo);
				ComplexFloat currentApprox = current.convertToFloat(exprSet());
				
				if(currentApprox.real < floatMax.real && currentApprox.real()) {
					floatMax = currentApprox;
					currentMax = current;
				}
			}
			return currentMax;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Range range = (Range)e;
			
			
			Func criticalPointsSet = getCriticalPoints(range.getExpr(),range.getVar(),casInfo);
			criticalPointsSet.println();
			
			{//remove criticalPoints outside domain
				ComplexFloat minApprox = range.getMin().convertToFloat(exprSet());
				ComplexFloat maxApprox = range.getMax().convertToFloat(exprSet());
				
				for(int i = 0;i<criticalPointsSet.size();i++) {
					if(criticalPointsSet.get(i).typeName().equals("solve")) {//Unsolved state
						criticalPointsSet.remove(i);
						i--;
						continue;
					}
					
					Expr critVal = Equ.getRightSide((Func)criticalPointsSet.get(i));
					ComplexFloat approx = critVal.convertToFloat(exprSet());
					
					if(approx.real > maxApprox.real || approx.real < minApprox.real) {//out of bounds of domain
						criticalPointsSet.remove(i);
						i--;
					}
					
				}
			}
			
			//add domain points to critical points list
			criticalPointsSet.add(equ(range.getVar(),range.getMin()));
			criticalPointsSet.add(equ(range.getVar(),range.getMax()));
			
			Expr min = minimum(range.getExpr(), criticalPointsSet,casInfo);
			Expr max = maximum(range.getExpr(), criticalPointsSet,casInfo);
			
			return sequence(min,max);
		}
	};
	
	static Rule mainSequenceRule = null;
	
	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				calculateRange
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return ComplexFloat.ZERO;
	}

	@Override
	public String typeName() {
		return "range";
	}

	@Override
	public String help() {
		return "range(min,max,expression,variable) function calculates the range of the expression under a domain\n"
				+ "examples\n"
				+ "range(-inf,inf,sin(x),x)->{-1,1}\n"
				+ "range(-1,1,x*sin(x)+x,x)";
	}
	
}
