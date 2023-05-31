package cas.primitive;

import java.math.BigInteger;
import java.util.ArrayList;

import cas.algebra.Solve;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.bool.*;

public class Abs{
	
	public static Func.FuncLoader absLoader = new Func.FuncLoader(){

		@Override
		public void load(Func owner) {
			Rule absOfPower = new Rule("abs(a^b)->abs(a)^b","abs of a power");
			Rule absOfAbs = new Rule("abs(abs(x))->abs(x)","absolute value of absolute value");
			Rule absOfEpsilon = new Rule("abs(epsilon)->epsilon","absolute value of epsilon becomes epsilon");
			
			/*
			 * checks for when the inside is a polynomial that never crosses the x axis
			 * it also handles things like 2*sin(x)+x^2+2 is always positive because 2*sin(x) has a max of 2 and the polynomial is always above 2
			 */
			Rule alwaysPositive = new Rule("expression is always positive") {
				Expr computeResult(Func abs,Expr theoryMin,Expr theoryMax,CasInfo casInfo) {
					boolean minPos = theoryMin.convertToFloat(exprSet()).real>=0 || theoryMin.equals(Num.ZERO);
					boolean minNeg = theoryMin.convertToFloat(exprSet()).real<=0 || theoryMin.equals(Num.ZERO);
					
					boolean maxPos = theoryMax.convertToFloat(exprSet()).real>=0 || theoryMax.equals(Num.ZERO);
					boolean maxNeg = theoryMax.convertToFloat(exprSet()).real<=0 || theoryMax.equals(Num.ZERO);
					
					if(minPos && maxPos) {
						return abs.get();
					}else if(minNeg && maxNeg) {
						return neg(abs.get()).simplify(casInfo);
					}
					
					return abs;
				}
				
				Expr basicSinProd,basicCosProd,basicAbsProd;
				
				@Override
				public void init() {
					basicSinProd = createExpr("a*sin(x)");
					basicCosProd = createExpr("a*cos(x)");
					basicAbsProd = createExpr("a*abs(x)");
					
					cases = new Rule[] {
							new Rule("abs(sin(x)+x^2-x)->sin(x)+x^2-x","special case of abs"),
							new Rule("abs(-sin(x)-x^2+x)->sin(x)+x2n-x","special case of abs"),
							
							new Rule("abs(cos(x)+x^2)->cos(x)+x^2","special case of abs"),
							new Rule("abs(a*cos(x)+a*x^2)->abs(a)*cos(x)+abs(a)*x^2","special case of abs"),
							
							new Rule("abs(cos(x)+x^2-x)->cos(x)+x^2-x","special case of abs"),
							new Rule("abs(cos(x)+x^2+x)->cos(x)+x^2+x","special case of abs"),
							new Rule("abs(-cos(x)-x^2-x)->cos(x)+x^2+x","special case of abs"),
							new Rule("abs(-cos(x)-x^2+x)->cos(x)+x^2-x","special case of abs"),
							
							new Rule("abs(a*sin(x)+x^2+b*x)->a*sin(x)+x^2-a*x","comparison(a=-b)&comparison(a>-pi)&comparison(a<pi)","special case of abs"),
							new Rule("abs(a*sin(x)-x^2+b*x)->-a*sin(x)+x^2+a*x","comparison(a=-b)&comparison(a>-pi)&comparison(a<pi)","special case of abs"),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func abs = (Func)e;
					
					if(casInfo.allowComplexNumbers()) return abs;
					
					for(Rule rule:cases) {
						e = rule.applyRuleToExpr(abs, casInfo);
						if(!(e instanceof Func)) return e;
					}
					
					ArrayList<VarCount> varcounts = new ArrayList<VarCount>();
					abs.get().countVars(varcounts);
					if(varcounts.size() != 1) return abs;
					
					Var v = varcounts.get(0).v;
					
					Expr polynomialSum = sum();
					
					Func innerSum = Sum.cast(abs.get());
					
					Expr theoryMin = sum(),theoryMax = sum();
					
					for(int i = 0;i<innerSum.size();i++) {
						Expr current = innerSum.get(i);
						if(!current.contains(v)) {
							theoryMin.add(current);
							theoryMax.add(current);
						}else if(isPlainPolynomial(current,v)){
							polynomialSum.add(current);
						}else if(current.isType("acos")) {
							theoryMax.add(pi());
						}else if(current.isType("lambertW")) {
							theoryMin.add(num(-1));
						}else if(current.isType("sin") || current.isType("cos")) {
							theoryMin.add(num(-1));
							theoryMax.add(num(1));
						}else if(Rule.fastSimilarExpr(basicSinProd, current)) {
							Func equsSet = Rule.getEqusFromTemplate(basicSinProd, current);
							Expr a = Rule.getExprByName(equsSet, "a");
							
							if(!a.contains(v)) {
								theoryMin.add(neg(abs(a)));
								theoryMax.add(abs(a));
							}else return abs;
						}else if(Rule.fastSimilarExpr(basicCosProd, current)) {
							Func equsSet = Rule.getEqusFromTemplate(basicCosProd, current);
							Expr a = Rule.getExprByName(equsSet, "a");
							
							if(!a.contains(v)) {
								theoryMin.add(neg(abs(a)));
								theoryMax.add(abs(a));
							}else return abs;
						}else if(current instanceof Func) {
							theoryMax.add(inf());
						}else if(Rule.fastSimilarExpr(basicAbsProd, current)) {
							Func equsSet = Rule.getEqusFromTemplate(basicAbsProd, current);
							Expr a = Rule.getExprByName(equsSet, "a");
							
							if(!a.contains(v)) {
								if( comparison(equGreater(a,num(0))).simplify(casInfo).equals(BoolState.TRUE) ) {
									theoryMax.add(inf());
								}else {
									theoryMin.add(neg(inf()));
								}
							}else return abs;
						}else {
							return abs;
						}
					}
					theoryMin = theoryMin.simplify(casInfo);
					theoryMax = theoryMax.simplify(casInfo);
					
					if(polynomialSum.size()==0) {
						return computeResult(abs,theoryMin,theoryMax,casInfo);
					}
					
					BigInteger degree = degree(polynomialSum,v);
					
					if(degree.mod(BigInteger.TWO).equals(BigInteger.ONE)) return abs;

					Func polySequence = polyExtract(polynomialSum,v,casInfo);
					boolean positive = polySequence.get(polySequence.size()-1).convertToFloat(exprSet()).real>0;
					if(positive) theoryMax = inf();
					else theoryMin = neg(inf());
					
					ArrayList<Double> derivPolySols = Solve.polySolve( polyExtract(diff(polynomialSum,v).simplify(casInfo),v,casInfo) );
					double polyMin = 0.0;
					double polyMax = 0.0;
					for(double solution:derivPolySols) {
						double out = polynomialSum.convertToFloat(exprSet( equ(v,floatExpr(solution)) )).real;
						
						polyMin = Math.min(polyMin, out);
						polyMax = Math.max(polyMax, out);
						
					}
					
					if(positive) theoryMin = floatExpr(sum(theoryMin,floatExpr(polyMin)).convertToFloat(exprSet()));
					else floatExpr(sum(theoryMax,floatExpr(polyMax)).convertToFloat(exprSet()));
					
					return computeResult(abs,theoryMin,theoryMax,casInfo);
				}
			};
			
			Rule absOfProd = new Rule("contains product") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func abs = (Func)e;
					
					if(abs.get().isType("prod")) {
						Func innerProd = (Func)abs.get();
						
						for(int i = 0;i<innerProd.size();i++) {
							innerProd.set(i, abs(innerProd.get(i)) );
						}
						
						return innerProd.simplify(casInfo);
					}
					
					return abs;
				}
			};
			
			Rule absOfDiv = new Rule("abs(a/b)->abs(a)/abs(b)","abs of a division");
			
			Rule allowAbsRule = new Rule("allow abs rule") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func abs = (Func)e;
					
					if(!casInfo.allowAbs()) return abs.get();
					
					return abs;
				}
			};
			
			Rule absOfNegConst = new Rule("abs of negative constant") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func abs = (Func)e;
					if(!abs.get().containsVars() && !abs.contains(Var.EPSILON)) {
						ComplexFloat approx = abs.get().convertToFloat(exprSet());
						if(ComplexFloat.closeToZero(approx.imag)) {
							if(approx.real<0) {
								return neg(abs.get()).simplify(casInfo);
							}
							return abs.get();
						}
					}
					return abs;
				}
			};
			
			Rule absOfComplexExpr = new Rule("abs of complex expression") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func abs = (Func)e;
					
					Func sepSequence = basicRealAndImagComponents(abs.get(),casInfo);
					
					if(!sepSequence.get(1).equals(Num.ZERO)) {
						return sqrt( sum(power(sepSequence.get(0),num(2)) , power(sepSequence.get(1),num(2))) ).simplify(casInfo);
					}
					
					return abs;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
					allowAbsRule,
					StandardRules.factorInner,
					absOfAbs,
					alwaysPositive,
					absOfNegConst,
					absOfEpsilon,
					absOfPower,
					absOfProd,
					absOfDiv,
					absOfComplexExpr
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.mag(owner.get().convertToFloat(varDefs));
				}
			};
		}
		
	};

}
