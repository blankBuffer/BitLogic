package cas.calculus;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.*;

public class Range{
	
	public static Func.FuncLoader rangeLoader = new Func.FuncLoader() {

		@Override
		public void load(Func owner) {
			Rule calculateRange = new Rule("calculate the range from critical points") {
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
					Func range = (Func)e;
					
					
					Func criticalPointsSet = getCriticalPoints(Range.getExpr(range),Range.getVar(range),casInfo);
					//criticalPointsSet.println();
					
					{//remove criticalPoints outside domain
						ComplexFloat minApprox = Range.getMin(range).convertToFloat(exprSet());
						ComplexFloat maxApprox = Range.getMax(range).convertToFloat(exprSet());
						
						for(int i = 0;i<criticalPointsSet.size();i++) {
							if(criticalPointsSet.get(i).isType("solve")) {//Unsolved state
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
					criticalPointsSet.add(equ(Range.getVar(range),Range.getMin(range)));
					criticalPointsSet.add(equ(Range.getVar(range),Range.getMax(range)));
					
					Expr min = minimum(Range.getExpr(range), criticalPointsSet,casInfo);
					Expr max = maximum(Range.getExpr(range), criticalPointsSet,casInfo);
					
					return sequence(min,max);
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					calculateRange
			},"main sequence");
			
			
		}
		
	};
	
	public static Expr getMin(Func range) {
		return range.get(0);
	}
	
	public static Expr getMax(Func range) {
		return range.get(1);
	}
	
	public static Var getVar(Func range) {
		return (Var)range.get(3);
	}
	
	public static Expr getExpr(Func range) {
		return range.get(2);
	}
	
}
