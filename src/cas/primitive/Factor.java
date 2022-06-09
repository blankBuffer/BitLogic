package cas.primitive;

import java.math.BigInteger;
import java.util.ArrayList;

import cas.*;


public class Factor extends Expr{
	
	private static final long serialVersionUID = -5448276275686292911L;
	
	static Rule sumOfCubes = new Rule("factor(a^3+b^3)->(a+b)*(a^2-a*b+b^2)","sum of cubes");
	static Rule differenceOfCubes = new Rule("factor(a^3-b^3)->(a-b)*(a^2+a*b+b^2)","difference of cubes");

	public Factor(){}//
	public Factor(Expr expr) {
		add(expr);
	}
	
	static Rule fastEscape = new Rule("nothing to factor") {
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Var || e.get() instanceof Num) return e.get();
			return e;
		}
	};
	
	static Rule reversePascalsTriangle = new Rule("reverse pascals triangle"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Factor factor = (Factor)e;
			Expr expr = factor.get();
			
			Var v = mostCommonVar(expr);
			if(expr instanceof Sum && v != null && isPlainPolynomial(expr,v )) {
				
				Sequence coefs = polyExtract(expr,v,casInfo);
				if(coefs == null) return e;
				Num degree = num(coefs.size()-1);
				if(degree.realValue.compareTo(BigInteger.TWO) == -1) return e;
				
				if(coefs.containsType("sum")) return e;
				
				Expr highestDegreeCoef = coefs.get(coefs.size()-1);
				Expr lowestDegreeCoef = coefs.get(0);
				
				Expr m = pow(highestDegreeCoef ,inv(degree)).simplify(casInfo);
				Expr b = pow(lowestDegreeCoef ,inv(degree)).simplify(casInfo);
				
				if(multinomial(sum(prod(m,v),b),degree,casInfo).equals(expr) ) {
					Expr result = pow(sum(prod(m,v),b),degree).simplify(casInfo);
					return result;
				}else if(multinomial(sum(prod(m,v),neg(b)),degree,casInfo).equals(expr)) {//try the negative variant
					Expr result = pow(sub(prod(m,v),b),degree).simplify(casInfo);
					return result;
				}
				
			}
			return e;
		}
	};
	
	static Rule power2Reduction = new Rule("power of 2 polynomial"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Factor factor = (Factor)e;
			Expr expr = factor.get();
			Var v = mostCommonVar(expr);
			if(expr instanceof Sum && v!=null && expr.size() == 2 && isPlainPolynomial(expr,v)) {
				Power pow = null;
				Expr other = null;
				if(expr.get(0) instanceof Power) {
					pow = (Power)expr.get(0);
					other = expr.get(1);
				}else if(expr.get(1) instanceof Power) {
					pow = (Power)expr.get(1);
					other = expr.get(0);
				}
				
				
				if(pow != null && other != null && other.negative() && isPositiveRealNum(pow.getExpo()) && ((Num)pow.getExpo()).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO) ) {
					CasInfo noAbsVersion = new CasInfo(casInfo);
					noAbsVersion.setAllowAbs(false);
					
					Expr newPow = sqrt(pow).simplify(noAbsVersion);
					Expr newOther = sqrt(neg(other)).simplify(noAbsVersion);
					
					if(!(newOther instanceof Power || newOther instanceof Prod)) {
						
						return prod(sum(newPow,newOther),sum(newPow,neg(newOther))).simplify(casInfo);
						
					}
					
				}
			}
			return e;
		}
	};
	
	static Rule quadraticFactor = new Rule("factor quadratics"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Factor factor = (Factor)e;
			Expr expr = factor.get();
			
			
			if(expr instanceof Sum) {
				Sequence coefs = null;
				Expr x = mostCommonVar(expr);
				if(x!=null) {
					coefs = polyExtract(expr, (Var)x,casInfo);
				}
				
				if(coefs != null) {
					if(coefs.size() >= 3 && coefs.size()%2 == 1) {//quadratic form
						//check discriminant
						for(int i = 0;i<coefs.size();i++) {
							if(!(coefs.get(i) instanceof Num)) {
								return e;
							}else if(i != 0&& i != coefs.size()-1 && i != (coefs.size()-1)/2 && !coefs.get(i).equals(Num.ZERO)) {//all between coefficients should be zero
								return e;
							}
						}
						Num a =  (Num)coefs.get(coefs.size()-1),b = (Num)coefs.get((coefs.size()-1)/2),c = (Num)coefs.get(0);
						
						if(!casInfo.allowComplexNumbers() && (a.isComplex() || b.isComplex() || c.isComplex())) return e;
						
						Num discrNum = (Num)sum(pow(b,num(2)),prod(num(-4),a,c)).simplify(casInfo);
						
						if( !isPositiveRealNum(discrNum) && !casInfo.allowComplexNumbers()) return e;
						
						boolean createsComplex = discrNum.isComplex() || discrNum.realValue.signum() == -1;
						
						if(createsComplex && !casInfo.allowComplexNumbers() ) return e;
						
						Expr discrNumSqrt = sqrt(discrNum).simplify(casInfo);
						
						if(!(discrNumSqrt instanceof Num)) return e;
							
							
							Expr out = new Prod();
							
							x = pow(x,num((coefs.size()-1)/2));
							
							Prod twoAX = prod(num(2),a,x);
							out.add( sum(twoAX,b.copy(),prod(num(-1),discrNumSqrt)) );
							out.add( sum(twoAX.copy(),b.copy(),discrNumSqrt.copy()) );
							out.add(inv(num(4)));
							out.add(inv(a.copy()));
							
							return out.simplify(casInfo);
						
							
						
					}
				}
			}
			
			return e;
		}
	};
	
	static Rule generalFactor = new Rule("general factor"){
		private static final long serialVersionUID = 1L;
		
		private Num getNumerOfPower(Power pow) {
			if(pow.getExpo() instanceof Num) {
				return (Num)pow.getExpo();
			}else if(pow.getExpo() instanceof Div && ((Div)pow.getExpo()).isNumericalAndReal() ) {
				return (Num)((Div)pow.getExpo()).getNumer();
			}else {
				return num(1);
			}
		}
		
		private Num getDenomOfPower(Power pow) {
			if(pow.getExpo() instanceof Div && ((Div)pow.getExpo()).isNumericalAndReal() ) {
				return (Num)((Div)pow.getExpo()).getDenom();
			}
			return num(1);
		}

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Factor factor = (Factor)e;
			if(e.contains(Var.INF)) return factor;//can't factor infinity lmao
			Expr expr = factor.get();
		
			if(expr instanceof Sum) {
				boolean sumHasDiv = false;
				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i) instanceof Div) {
						sumHasDiv = true;
						break;
					}
				}
				if(sumHasDiv){//combine fractions
					Div sum = div(num(0),num(1));
					for(int i = 0;i<expr.size();i++) {
						Div current = Div.cast(expr.get(i));
						sum = Div.addFracs(sum, current);
					}
					return sum.simplify(casInfo);
				}
				Prod leadingTerm = Prod.cast(expr.get(0));
				Num leadingTermCoef = (Num)leadingTerm.getCoefficient();
				BigInteger gcd = leadingTermCoef.gcd();
				Expr factors = new Prod();
				//calculate gcd
				for(int i = 1;i<expr.size();i++) {
					gcd = ((Num)expr.get(i).getCoefficient()).gcd().gcd(gcd);
				}
				if(expr.negative()) gcd = gcd.negate();
				//add to factors product
				if(!gcd.equals(BigInteger.ONE)) factors.add(num(gcd));
				//common term
				for(int i = 0;i<leadingTerm.size();i++) {
					Expr subTerm = leadingTerm.get(i);
					if(subTerm instanceof Num) continue;
					
					Power termPower = Power.cast(subTerm);
					
					if(!Div.cast(termPower.getExpo()).isNumericalAndReal()) {
						Sequence parts = seperateCoef(termPower.getExpo());
						termPower = pow(pow(termPower.getBase(),parts.get(1)),parts.get(0));
					}
					
					Num minExpoNum = getNumerOfPower(termPower);
					Num minExpoDen = getDenomOfPower(termPower);
					
					
					for(int j = 1;j<expr.size();j++) {
						Prod otherTerm = Prod.cast(expr.get(j));
						
						boolean found = false;
						
						for(int k = 0;k<otherTerm.size();k++) {
							Expr otherSubTerm = otherTerm.get(k);
							if(otherSubTerm instanceof Num) continue;
							
							Power otherTermPower = Power.cast(otherSubTerm);
							
							if(!Div.cast(otherTermPower.getExpo()).isNumericalAndReal()) {
								Sequence parts = seperateCoef(otherTermPower.getExpo());
								otherTermPower = pow(pow(otherTermPower.getBase(),parts.get(1)),parts.get(0));
							}
							
							if(otherTermPower.getBase().equals(termPower.getBase())) {
								found = true;
								
								Num expoNum = getNumerOfPower(otherTermPower);
								Num expoDen = getDenomOfPower(otherTermPower);
								
								BigInteger a = minExpoNum.realValue.multiply(expoDen.realValue);
								BigInteger b = expoNum.realValue.multiply(minExpoDen.realValue);
								
								if(b.compareTo(a) == -1) {
									minExpoNum = expoNum;
									minExpoDen = expoDen;
								}
								
								break;
							}
							
						}
						if(!found) {
							minExpoNum = Num.ZERO;
							break;
						}
					}
					if(!minExpoNum.equals(Num.ZERO)) {
						factors.add( Power.unCast( pow(termPower.getBase(), Div.unCast(div(minExpoNum,minExpoDen)))));
					}
				}
				
				//
				if(factors.size()>0) {
					//divide terms by factors
					//System.out.println(expr+" "+factors);
					
					for(int i = 0;i<expr.size();i++) {
						expr.set(i, div(expr.get(i),factors).simplify(casInfo));
					}
					expr = Prod.combine(factors, factor(expr).simplify(casInfo));
					
					return expr;
				}
			}
			
			
			return e;
		}
	};
	
	static Rule reExpandSubSums = new Rule("re distribute sums"){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Factor factor = (Factor)e;
			Expr expr = factor.get();
			
			if(expr instanceof Prod) {

				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i) instanceof Sum) {
						Expr subSum = expr.get(i);
						subSum.flags.simple = false;
						expr.set(i, subSum.simplify(casInfo));
					}
				}
			}
			
			return e;
		}
	};
	
	static Rule pullOutRoots = new Rule("pull out roots of polynomial"){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Factor factor = (Factor)e;
			Expr expr = factor.get();
			
			Var v = mostCommonVar(expr);
			
			if(expr instanceof Sum && v != null && isPlainPolynomial(expr,v) ) {
				int degree = degree(expr,v).intValue();
				if(degree < 2) return e;
				Sequence poly = polyExtract(expr,v,casInfo);
				if(poly == null) return e;
				for(int i = 0;i<poly.size();i++) if(!(poly.get(i) instanceof Num)) return e;//must be all nums
				ArrayList<Double> rootsAsFloat = Solve.polySolve(poly);
				Prod out = new Prod();
				//System.out.println(expr);
				//System.out.println(rootsAsFloat);
				for(double root:rootsAsFloat) {
					if(Double.isNaN(root)) return e;
					Sequence rootAsPoly = sequence();//the polynomial to be divided
					long[] frac =  toFraction(root);
					rootAsPoly.add(num(-frac[0]));
					
					Sequence[] divided = null;//divided[1] is the remainder
					
					//System.out.println("r "+root+" "+degree);
					for(int i = 1;i<Math.min(8, degree);i++) {//this checks other factors like x^3-7, still integer root but a quadratic 
						//System.out.println(frac[0]+"/"+frac[1]);
						rootAsPoly.add(num(frac[1]));
						divided = polyDiv(poly, rootAsPoly, casInfo);//try polynomial division
						if(divided[1].size() == 0) break;//success
						rootAsPoly.set(i, num(0));//shifting to next degree
						double newApprox = Math.pow(root,i+1);
						//System.out.println(newApprox);
						frac =  toFraction(newApprox);//shift to  next degree
						rootAsPoly.set(0, num(-frac[0]));//shift to  next degree
					}
					
					
					if(divided != null && divided[1].size() == 0) {
						out.add( exprListToPoly(rootAsPoly, v, casInfo) );
						poly = divided[0];
						degree = poly.size()-1;
					}
					
				}
				out.add( exprListToPoly(poly, v, casInfo) );
				if(out.size() > 1) {
					return out.simplify(casInfo);
				}
				
			}
			return e;
			
		}
	};
	
	static Sequence ruleSequence = null;
	public static void loadRules(){
		ruleSequence = sequence(
				fastEscape,
				sumOfCubes,
				differenceOfCubes,
				power2Reduction,
				quadraticFactor,
				pullOutRoots,
				reversePascalsTriangle,
				generalFactor,
				reExpandSubSums,
				StandardRules.becomeInner
					
		);
		Rule.initRules(ruleSequence);
	}
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

	@Override
	public String typeName() {
		return "factor";
	}
	@Override
	public String help() {
		return "factor(x) is the factor computer\n"
				+ "examples\n"
				+ "factor(x*a+x*b)->x*(a+b)\n"
				+ "factor(x^2-x-6)->(x-3)*(x+2)";
	}
	
}
