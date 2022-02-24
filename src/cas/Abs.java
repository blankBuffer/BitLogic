package cas;

import java.math.BigInteger;
import java.util.ArrayList;

public class Abs extends Expr{
	private static final long serialVersionUID = 2865185687344371868L;
	Abs() {}//
	
	public Abs(Expr e) {
		add(e);
	}
	
	static Rule absOfPower = new Rule("abs(a^b)->abs(a)^b","abs of a power",Rule.EASY);
	static Rule absOfAbs = new Rule("abs(abs(x))->abs(x)","absolute value of absolute value",Rule.VERY_EASY);
	
	
	/*
	 * checks for when the inside is a polynomial that never crosses the x axis
	 * it also handles things like 2*sin(x)+x^2+2 is always positive because 2*sin(x) has a max of 2 and the polynomial is always above 2
	 */
	static Rule alwaysPositive = new Rule("expression is always positive",Rule.UNCOMMON) {
		private static final long serialVersionUID = 1L;
		
		Expr computeResult(Abs abs,Expr theoryMin,Expr theoryMax,Settings settings) {
			boolean minPos = theoryMin.convertToFloat(exprList()).real>=0 || theoryMin.equals(Num.ZERO);
			boolean minNeg = theoryMin.convertToFloat(exprList()).real<=0 || theoryMin.equals(Num.ZERO);
			
			boolean maxPos = theoryMax.convertToFloat(exprList()).real>=0 || theoryMax.equals(Num.ZERO);
			boolean maxNeg = theoryMax.convertToFloat(exprList()).real<=0 || theoryMax.equals(Num.ZERO);
			
			if(minPos && maxPos) {
				return abs.get();
			}else if(minNeg && maxNeg) {
				return neg(abs.get()).simplify(settings);
			}
			
			return abs;
		}
		
		Rule[] cases;
		Expr basicSinProd,basicCosProd;
		
		@Override
		public void init() {
			basicSinProd = createExpr("a*sin(x)");
			basicCosProd = createExpr("a*cos(x)");
			
			cases = new Rule[] {
					new Rule("abs(sin(x)+x^2-x)->sin(x)+x^2-x","special case of abs",Rule.UNCOMMON),
					new Rule("abs(-sin(x)-x^2+x)->sin(x)+x2n-x","special case of abs",Rule.UNCOMMON),
					
					new Rule("abs(cos(x)+x^2)->cos(x)+x^2","special case of abs",Rule.UNCOMMON),
					new Rule("abs(a*cos(x)+a*x^2)->abs(a)*cos(x)+abs(a)*x^2","special case of abs",Rule.UNCOMMON),
					
					new Rule("abs(cos(x)+x^2-x)->cos(x)+x^2-x","special case of abs",Rule.UNCOMMON),
					new Rule("abs(cos(x)+x^2+x)->cos(x)+x^2+x","special case of abs",Rule.UNCOMMON),
					new Rule("abs(-cos(x)-x^2-x)->cos(x)+x^2+x","special case of abs",Rule.UNCOMMON),
					new Rule("abs(-cos(x)-x^2+x)->cos(x)+x^2-x","special case of abs",Rule.UNCOMMON),
					
					new Rule("abs(a*sin(x)+x^2+b*x)->a*sin(x)+x^2-a*x","eval(a=-b)&eval(a>-pi)&eval(a<pi)","special case of abs",Rule.UNCOMMON),
					new Rule("abs(a*sin(x)-x^2+b*x)->-a*sin(x)+x^2+a*x","eval(a=-b)&eval(a>-pi)&eval(a<pi)","special case of abs",Rule.UNCOMMON),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Abs abs = (Abs)e;
			
			for(Rule rule:cases) {
				e = rule.applyRuleToExpr(abs, settings);
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
				if(current instanceof Num) {
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
				}else if(Rule.fastSimilarStruct(basicSinProd, current)) {
					ExprList equs = Rule.getEqusFromTemplate(basicSinProd, current);
					Expr a = Rule.getExprByName(equs, "a");
					Expr x = Rule.getExprByName(equs, "x");
					
					if(x.contains(v)) {
						theoryMin.add(neg(abs(a)));
						theoryMax.add(abs(a));
					}else return abs;
				}else if(Rule.fastSimilarStruct(basicCosProd, current)) {
					ExprList equs = Rule.getEqusFromTemplate(basicCosProd, current);
					Expr a = Rule.getExprByName(equs, "a");
					Expr x = Rule.getExprByName(equs, "x");
					
					if(x.contains(v)) {
						theoryMin.add(neg(abs(a)));
						theoryMax.add(abs(a));
					}else return abs;
				}else {
					return abs;
				}
			}
			theoryMin = theoryMin.simplify(settings);
			theoryMax = theoryMax.simplify(settings);
			
			if(polynomialSum.size()==0) {
				return computeResult(abs,theoryMin,theoryMax,settings);
			}
			
			BigInteger degree = degree(polynomialSum,v);
			
			if(degree.mod(BigInteger.TWO).equals(BigInteger.ONE)) return abs;

			Sequence poly = polyExtract(polynomialSum,v,settings);
			boolean positive = poly.get(poly.size()-1).convertToFloat(exprList()).real>0;
			if(positive) theoryMax = inf();
			else theoryMin = neg(inf());
			
			ArrayList<Double> derivPolySols = Solve.polySolve( polyExtract(diff(polynomialSum,v).simplify(settings),v,settings) );
			double polyMin = 0.0;
			double polyMax = 0.0;
			for(double solution:derivPolySols) {
				double out = polynomialSum.convertToFloat(exprList( equ(v,floatExpr(solution)) )).real;
				
				polyMin = Math.min(polyMin, out);
				polyMax = Math.max(polyMax, out);
				
			}
			
			if(positive) theoryMin = floatExpr(sum(theoryMin,floatExpr(polyMin)).convertToFloat(exprList()));
			else floatExpr(sum(theoryMax,floatExpr(polyMax)).convertToFloat(exprList()));
			
			return computeResult(abs,theoryMin,theoryMax,settings);
		}
	};
	
	static Rule absOfProd = new Rule("contains product",Rule.UNCOMMON) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Abs abs = (Abs)e;
			
			if(abs.get() instanceof Prod) {
				Prod innerProd = (Prod)abs.get();
				
				for(int i = 0;i<innerProd.size();i++) {
					innerProd.set(i, abs(innerProd.get(i)) );
				}
				
				return innerProd.simplify(settings);
			}
			
			return abs;
		}
	};
	
	static Rule absOfDiv = new Rule("abs(a/b)->abs(a)/abs(b)","abs of a division",Rule.UNCOMMON);
	
	static Rule absOfNum = new Rule("abs of a number",Rule.VERY_EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Abs abs = (Abs)e;
			
			if(abs.get() instanceof Num) {
				Num num = (Num)abs.get();
				
				if(num.isComplex()) {
					return sqrt( num(  num.realValue.pow(2).add(num.imagValue.pow(2))  ) ).simplify(settings);
				}
				return num(num.realValue.abs());
			}
			
			return abs;
		}
	};
	
	static Rule allowAbsRule = new Rule("allow abs rule",Rule.VERY_EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Abs abs = (Abs)e;
			
			if(!settings.allowAbs) return abs.get();
			
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
				absOfNum,
				absOfPower,
				absOfProd,
				absOfDiv
		);
		Rule.initRules(ruleSequence);
	}
	@Override
	Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.mag(get().convertToFloat(varDefs));
	}

}
