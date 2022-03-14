package cas.primitive;

import java.math.BigInteger;
import java.util.ArrayList;

import cas.*;
import cas.bool.*;
import cas.special.LambertW;
import cas.trig.*;

public class Abs extends Expr{
	private static final long serialVersionUID = 2865185687344371868L;
	public Abs() {}//
	
	public Abs(Expr e) {
		add(e);
	}
	
	static Rule absOfPower = new Rule("abs(a^b)->abs(a)^b","abs of a power");
	static Rule absOfAbs = new Rule("abs(abs(x))->abs(x)","absolute value of absolute value");
	static Rule absOfEpsilon = new Rule("abs(epsilon)->epsilon","absolute value of epsilon becomes epsilon");
	
	/*
	 * checks for when the inside is a polynomial that never crosses the x axis
	 * it also handles things like 2*sin(x)+x^2+2 is always positive because 2*sin(x) has a max of 2 and the polynomial is always above 2
	 */
	static Rule alwaysPositive = new Rule("expression is always positive") {
		private static final long serialVersionUID = 1L;
		
		Expr computeResult(Abs abs,Expr theoryMin,Expr theoryMax,CasInfo casInfo) {
			boolean minPos = theoryMin.convertToFloat(exprList()).real>=0 || theoryMin.equals(Num.ZERO);
			boolean minNeg = theoryMin.convertToFloat(exprList()).real<=0 || theoryMin.equals(Num.ZERO);
			
			boolean maxPos = theoryMax.convertToFloat(exprList()).real>=0 || theoryMax.equals(Num.ZERO);
			boolean maxNeg = theoryMax.convertToFloat(exprList()).real<=0 || theoryMax.equals(Num.ZERO);
			
			if(minPos && maxPos) {
				return abs.get();
			}else if(minNeg && maxNeg) {
				return neg(abs.get()).simplify(casInfo);
			}
			
			return abs;
		}
		
		Rule[] cases;
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
					
					new Rule("abs(a*sin(x)+x^2+b*x)->a*sin(x)+x^2-a*x","eval(a=-b)&eval(a>-pi)&eval(a<pi)","special case of abs"),
					new Rule("abs(a*sin(x)-x^2+b*x)->-a*sin(x)+x^2+a*x","eval(a=-b)&eval(a>-pi)&eval(a<pi)","special case of abs"),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Abs abs = (Abs)e;
			
			for(Rule rule:cases) {
				e = rule.applyRuleToExpr(abs, casInfo);
				if(!(e instanceof Abs)) return e;
			}
			
			ArrayList<VarCount> varcounts = new ArrayList<VarCount>();
			abs.get().countVars(varcounts);
			if(varcounts.size() != 1) return abs;
			
			Var v = varcounts.get(0).v;
			
			Expr polynomialSum = new Sum();
			
			Sum innerSum = Sum.cast(abs.get());
			
			Expr theoryMin = sum(),theoryMax = sum();
			
			for(int i = 0;i<innerSum.size();i++) {
				Expr current = innerSum.get(i);
				if(!current.contains(v)) {
					theoryMin.add(current);
					theoryMax.add(current);
				}else if(isPlainPolynomial(current,v)){
					polynomialSum.add(current);
				}else if(current instanceof Acos) {
					theoryMax.add(pi());
				}else if(current instanceof LambertW) {
					theoryMin.add(num(-1));
				}else if(current instanceof Sin || current instanceof Cos) {
					theoryMin.add(num(-1));
					theoryMax.add(num(1));
				}else if(Rule.fastSimilarExpr(basicSinProd, current)) {
					ExprList equs = Rule.getEqusFromTemplate(basicSinProd, current);
					Expr a = Rule.getExprByName(equs, "a");
					
					if(!a.contains(v)) {
						theoryMin.add(neg(abs(a)));
						theoryMax.add(abs(a));
					}else return abs;
				}else if(Rule.fastSimilarExpr(basicCosProd, current)) {
					ExprList equs = Rule.getEqusFromTemplate(basicCosProd, current);
					Expr a = Rule.getExprByName(equs, "a");
					
					if(!a.contains(v)) {
						theoryMin.add(neg(abs(a)));
						theoryMax.add(abs(a));
					}else return abs;
				}else if(current instanceof Abs) {
					theoryMax.add(inf());
				}else if(Rule.fastSimilarExpr(basicAbsProd, current)) {
					ExprList equs = Rule.getEqusFromTemplate(basicAbsProd, current);
					Expr a = Rule.getExprByName(equs, "a");
					
					if(!a.contains(v)) {
						if( eval(equGreater(a,num(0))).simplify(casInfo).equals(BoolState.TRUE) ) {
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

			Sequence poly = polyExtract(polynomialSum,v,casInfo);
			boolean positive = poly.get(poly.size()-1).convertToFloat(exprList()).real>0;
			if(positive) theoryMax = inf();
			else theoryMin = neg(inf());
			
			ArrayList<Double> derivPolySols = Solve.polySolve( polyExtract(diff(polynomialSum,v).simplify(casInfo),v,casInfo) );
			double polyMin = 0.0;
			double polyMax = 0.0;
			for(double solution:derivPolySols) {
				double out = polynomialSum.convertToFloat(exprList( equ(v,floatExpr(solution)) )).real;
				
				polyMin = Math.min(polyMin, out);
				polyMax = Math.max(polyMax, out);
				
			}
			
			if(positive) theoryMin = floatExpr(sum(theoryMin,floatExpr(polyMin)).convertToFloat(exprList()));
			else floatExpr(sum(theoryMax,floatExpr(polyMax)).convertToFloat(exprList()));
			
			return computeResult(abs,theoryMin,theoryMax,casInfo);
		}
	};
	
	static Rule absOfProd = new Rule("contains product") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Abs abs = (Abs)e;
			
			if(abs.get() instanceof Prod) {
				Prod innerProd = (Prod)abs.get();
				
				for(int i = 0;i<innerProd.size();i++) {
					innerProd.set(i, abs(innerProd.get(i)) );
				}
				
				return innerProd.simplify(casInfo);
			}
			
			return abs;
		}
	};
	
	static Rule absOfDiv = new Rule("abs(a/b)->abs(a)/abs(b)","abs of a division");
	
	static Rule allowAbsRule = new Rule("allow abs rule") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Abs abs = (Abs)e;
			
			if(!casInfo.allowAbs()) return abs.get();
			
			return abs;
		}
	};
	
	static Rule absOfNegConst = new Rule("abs of negative constant") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Abs abs = (Abs)e;
			if(!abs.get().containsVars() && !abs.contains(Var.EPSILON)) {
				ComplexFloat approx = abs.get().convertToFloat(exprList());
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
	
	static Rule absOfComplexExpr = new Rule("abs of complex expression") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Abs abs = (Abs)e;
			
			Sequence sep = basicRealAndImagComponents(abs.get(),casInfo);
			
			if(!sep.get(1).equals(Num.ZERO)) {
				return sqrt( sum(pow(sep.get(0),num(2)) , pow(sep.get(1),num(2))) ).simplify(casInfo);
			}
			
			return abs;
		}
	};

	static Sequence ruleSequence;
	public static void loadRules(){
		ruleSequence = sequence(
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
		);
		Rule.initRules(ruleSequence);
	}
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.mag(get().convertToFloat(varDefs));
	}

	@Override
	public String typeName() {
		return "abs";
	}

}
