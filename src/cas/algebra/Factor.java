package cas.algebra;

import java.math.BigInteger;
import java.util.ArrayList;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.primitive.*;


public class Factor{
	
	public static Func.FuncLoader factorLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule(new Rule[]{
					fastEscape,
					sumOfCubes,
					differenceOfCubes,
					power2Reduction,
					quarticRealFactor,
					quadraticFactor,
					pullOutRoots,
					reversePascalsTriangle,
					generalFactor,
					reExpandSubSums,
					StandardRules.becomeInner		
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return owner.get().convertToFloat(varDefs);
				}
			};
		}
	};
	
	static Rule sumOfCubes = new Rule("factor(a^3+b^3)->(a+b)*(a^2-a*b+b^2)","sum of cubes");
	static Rule differenceOfCubes = new Rule("factor(a^3-b^3)->(a-b)*(a^2+a*b+b^2)","difference of cubes");
	
	static Rule fastEscape = new Rule("nothing to factor") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Var || e.get() instanceof Num) return e.get();
			return e;
		}
	};
	
	static Rule reversePascalsTriangle = new Rule("reverse pascals triangle"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			Expr expr = factor.get();
			
			Var v = mostCommonVar(expr);
			if(expr.typeName().equals("sum") && v != null && isPlainPolynomial(expr,v )) {
				
				Sequence coefs = polyExtract(expr,v,casInfo);
				if(coefs == null) return e;
				Num degree = num(coefs.size()-1);
				if(degree.getRealValue().compareTo(BigInteger.TWO) == -1) return e;
				
				if(coefs.containsType("sum")) return e;
				
				Expr highestDegreeCoef = coefs.get(coefs.size()-1);
				Expr lowestDegreeCoef = coefs.get(0);
				
				Expr m = power(highestDegreeCoef ,inv(degree)).simplify(casInfo);
				Expr b = power(lowestDegreeCoef ,inv(degree)).simplify(casInfo);
				
				if(multinomial(sum(prod(m,v),b),degree,casInfo).equals(expr) ) {
					Expr result = power(sum(prod(m,v),b),degree).simplify(casInfo);
					return result;
				}else if(multinomial(sum(prod(m,v),neg(b)),degree,casInfo).equals(expr)) {//try the negative variant
					Expr result = power(sub(prod(m,v),b),degree).simplify(casInfo);
					return result;
				}
				
			}
			return e;
		}
	};
	
	/*
	 * if the polynomial is in the form 
	 * (x^n-b) such that n is even, then it becomes
	 * (x^(n/2)-sqrt(b))*(x^(n/2)+sqrt(b))
	 * 
	 * there are some conditions to avoid square roots in the answer
	 * 
	 * TODO
	 * potential issues factor(x^4-b^2) will work but factor(x^4-b^4) might not work
	 * also it might be incompatible with the form -x^4+b since -x^4 wont be detected as a power
	 * 
	 * 
	 */
	static Rule power2Reduction = new Rule("power of 2 polynomial"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			Expr expr = factor.get();
			
			//get the variable we are working with
			Var v = mostCommonVar(expr);
			
			//if its a sum of length 2 and is in polynomial form
			if(expr.typeName().equals("sum") && v!=null && expr.size() == 2 && isPlainPolynomial(expr,v)) {
				Func pow = null;
				Expr other = null;
				//find x^n and b, two cases x^n+b or b+x^n
				if(expr.get(0).typeName().equals("power")) {
					pow = (Func)expr.get(0);
					other = expr.get(1);
				}else if(expr.get(1).typeName().equals("power")) {
					pow = (Func)expr.get(1);
					other = expr.get(0);
				}
				
				//check if found and if b is negative and if exponent mod 2 is zero AKA even
				if(pow != null && other != null && other.negative() && isPositiveRealNum(pow.getExpo()) && ((Num)pow.getExpo()).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO) ) {
					//disable absolute value mode, example we don't want sqrt(x^2) to become abs(x) we just want x
					CasInfo noAbsVersion = new CasInfo(casInfo);
					noAbsVersion.setAllowAbs(false);
					
					Expr newPow = sqrt(pow).simplify(noAbsVersion);//sqrt(x^n) = x^(n/2)
					//the neg is there because the b term is technically (-b) so we have to make it positive again to avoid complex numbers
					Expr newOther = sqrt(neg(other)).simplify(noAbsVersion);//sqrt(b)
					
					/*
					 * make sure sqrt(b) does not result in a root or product as that increases complexity
					 * why might it become a product? remember b could be an integer and that sqrt(12) results in 2*sqrt(3)
					 */
					
					if(!(newOther.typeName().equals("power") || newOther.typeName().equals("prod"))) {
						
						return prod(sum(newPow,newOther),sum(newPow,neg(newOther))).simplify(casInfo);
						
					}
					
				}
			}
			return e;
		}
	};
	
	static Rule quarticRealFactor = new Rule(new Rule[] {
			new Rule("factor(b*x^4+a)->(sqrt(b)*x^2-b^(1/4)*a^(1/4)*sqrt(2)*x+sqrt(a))*(sqrt(b)*x^2+b^(1/4)*a^(1/4)*sqrt(2)*x+sqrt(a))","factorIrrationalRoots()&comparison(a>0)&comparison(b>0)","factor quartics using special technique"),
			new Rule("factor(x^4+a)->(x^2-a^(1/4)*sqrt(2)*x+sqrt(a))*(x^2+a^(1/4)*sqrt(2)*x+sqrt(a))","factorIrrationalRoots()&comparison(a>0)","factor quartics using special technique"),
	},"factor quartics using special technique");
	
	static Rule quadraticFactor = new Rule("factor quadratics"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			Expr expr = factor.get();
			
			
			if(expr.typeName().equals("sum")) {
				Sequence coefs = null;
				Expr x = mostCommonVar(expr);
				if(x!=null) {
					coefs = polyExtract(expr, (Var)x,casInfo);
				}
				
				if(coefs != null) {
					if(coefs.size() >= 3 && coefs.size()%2 == 1) {//quadratic form
						//check discriminant
						for(int i = 0;i<coefs.size();i++) {
							if(!(coefs.get(i) instanceof Num || casInfo.factorIrrationalRoots())) {//should be a num or factoring irrational roots is allowed
								return e;
							}else if(i != 0&& i != coefs.size()-1 && i != (coefs.size()-1)/2 && !coefs.get(i).equals(Num.ZERO)) {//all between coefficients should be zero
								return e;
							}
						}
						Expr a =  coefs.get(coefs.size()-1),b = coefs.get((coefs.size()-1)/2),c = coefs.get(0);
						
						if(!casInfo.allowComplexNumbers() && ( (a instanceof Num && ((Num)a).isComplex()) || (b instanceof Num && ((Num)b).isComplex()) || (c instanceof Num && ((Num)c).isComplex()) )) return e;
						
						Expr discrNum = sum(power(b,num(2)),prod(num(-4),a,c)).simplify(casInfo);
						
						if( !(isPositiveRealNum(discrNum) || casInfo.allowComplexNumbers() || (!discrNum.negative() && casInfo.factorIrrationalRoots()) ) ) return e;
						
						boolean createsComplex = discrNum instanceof Num && (((Num)discrNum).isComplex() || ((Num)discrNum).getRealValue().signum() == -1);
						
						if(createsComplex && !casInfo.allowComplexNumbers() ) return e;
						
						Expr discrNumSqrt = sqrt(discrNum).simplify(casInfo);
						
						if(discrNumSqrt instanceof Num || casInfo.factorIrrationalRoots()) {
							
							
							Func outProd = prod();
							
							x = power(x,num((coefs.size()-1)/2));
							
							Func twoAXProd = prod(num(2),a,x);
							outProd.add( sum(twoAXProd,b.copy(),prod(num(-1),discrNumSqrt)) );
							outProd.add( sum(twoAXProd.copy(),b.copy(),discrNumSqrt.copy()) );
							outProd.add(inv(num(4)));
							outProd.add(inv(a.copy()));
							
							return outProd.simplify(casInfo);
						}
							
						
					}
				}
			}
			
			return e;
		}
	};
	
	static Rule generalFactor = new Rule("general factor"){
		private Num getNumerOfPower(Func pow) {
			if(pow.getExpo() instanceof Num) {
				return (Num)pow.getExpo();
			}else if(pow.getExpo().typeName().equals("div") && Div.isNumericalAndReal((Func)pow.getExpo()) ) {
				return (Num)((Func)pow.getExpo()).getNumer();
			}else {
				return num(1);
			}
		}
		
		private Num getDenomOfPower(Func pow) {
			if(pow.getExpo().typeName().equals("div") && Div.isNumericalAndReal((Func)pow.getExpo()) ) {
				return (Num)((Func)pow.getExpo()).getDenom();
			}
			return num(1);
		}

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			if(e.contains(Var.INF)) return factor;//can't factor infinity lmao
			Expr expr = factor.get();
		
			if(expr.typeName().equals("sum")) {
				boolean sumHasDiv = false;
				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i).typeName().equals("div")) {
						sumHasDiv = true;
						break;
					}
				}
				if(sumHasDiv){//combine fractions
					Func sum = div(num(0),num(1));
					for(int i = 0;i<expr.size();i++) {
						Func current = Div.cast(expr.get(i));
						sum = Div.addFracs(sum, current);
					}
					return sum.simplify(casInfo);
				}
				Func leadingTermProd = Prod.cast(expr.get(0));
				Num leadingTermCoef = (Num)leadingTermProd.getCoefficient();
				BigInteger gcd = leadingTermCoef.gcd();
				Expr factors = prod();
				//calculate gcd
				for(int i = 1;i<expr.size();i++) {
					gcd = ((Num)expr.get(i).getCoefficient()).gcd().gcd(gcd);
				}
				if(expr.negative()) gcd = gcd.negate();
				//add to factors product
				if(!gcd.equals(BigInteger.ONE)) factors.add(num(gcd));
				//common term
				for(int i = 0;i<leadingTermProd.size();i++) {
					Expr subTerm = leadingTermProd.get(i);
					if(subTerm instanceof Num) continue;
					
					Func termPower = Power.cast(subTerm);
					
					if(!Div.isNumericalAndReal(Div.cast(termPower.getExpo()))) {
						Sequence parts = seperateCoef(termPower.getExpo());
						termPower = power(power(termPower.getBase(),parts.get(1)),parts.get(0));
					}
					
					Num minExpoNum = getNumerOfPower(termPower);
					Num minExpoDen = getDenomOfPower(termPower);
					
					
					for(int j = 1;j<expr.size();j++) {
						Func otherTermProd = Prod.cast(expr.get(j));
						
						boolean found = false;
						
						for(int k = 0;k<otherTermProd.size();k++) {
							Expr otherSubTerm = otherTermProd.get(k);
							if(otherSubTerm instanceof Num) continue;
							
							Func otherTermPower = Power.cast(otherSubTerm);
							
							if(!Div.isNumericalAndReal(Div.cast(otherTermPower.getExpo()))) {
								Sequence parts = seperateCoef(otherTermPower.getExpo());
								otherTermPower = power(power(otherTermPower.getBase(),parts.get(1)),parts.get(0));
							}
							
							if(otherTermPower.getBase().equals(termPower.getBase())) {
								found = true;
								
								Num expoNum = getNumerOfPower(otherTermPower);
								Num expoDen = getDenomOfPower(otherTermPower);
								
								BigInteger a = minExpoNum.getRealValue().multiply(expoDen.getRealValue());
								BigInteger b = expoNum.getRealValue().multiply(minExpoDen.getRealValue());
								
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
						factors.add( Power.unCast( power(termPower.getBase(), Div.unCast(div(minExpoNum,minExpoDen)))));
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			Expr expr = factor.get();
			
			if(expr.typeName().equals("prod")) {

				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i).typeName().equals("sum")) {
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			Expr expr = factor.get();
			
			Var v = mostCommonVar(expr);
			
			if(expr.typeName().equals("sum") && v != null && isPlainPolynomial(expr,v) ) {
				int degree = degree(expr,v).intValue();
				if(degree < 2) return e;
				Sequence poly = polyExtract(expr,v,casInfo);
				if(poly == null) return e;
				for(int i = 0;i<poly.size();i++) if(!(poly.get(i) instanceof Num)) return e;//must be all nums
				ArrayList<Double> rootsAsFloat = Solve.polySolve(poly);
				Func outProd = prod();
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
						outProd.add( exprListToPoly(rootAsPoly, v, casInfo) );
						poly = divided[0];
						degree = poly.size()-1;
					}
					
				}
				outProd.add( exprListToPoly(poly, v, casInfo) );
				if(outProd.size() > 1) {
					return outProd.simplify(casInfo);
				}
				
			}
			return e;
			
		}
	};
}
