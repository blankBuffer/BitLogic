package cas.algebra;

import java.math.BigInteger;
import java.util.ArrayList;

import cas.Algorithms;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.primitive.*;

import static cas.Cas.*;


public class Factor{
	
	public static Func.FuncLoader factorLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Factors expressions using different methods.\n"
					+ "For example factor(x^2+2*x+1) returns (x+1)^2\n"
					+ "It also compresses fractions back into one fraction like factor(x/3+y/4) returns (4*x+3*y)/12";
			
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
			
			Var v = Algorithms.mostCommonVar(expr);
			if(expr.isType("sum") && v != null && Algorithms.isPlainPolynomial(expr,v )) {
				
				Func coefsSequence = Algorithms.polyExtract(expr,v,casInfo);
				if(coefsSequence == null) return e;
				Num degree = num(coefsSequence.size()-1);
				if(degree.getRealValue().compareTo(BigInteger.TWO) == -1) return e;
				
				if(coefsSequence.containsType("sum")) return e;
				
				Expr highestDegreeCoef = coefsSequence.get(coefsSequence.size()-1);
				Expr lowestDegreeCoef = coefsSequence.get(0);
				
				Expr m = power(highestDegreeCoef ,inv(degree)).simplify(casInfo);
				Expr b = power(lowestDegreeCoef ,inv(degree)).simplify(casInfo);
				
				if(Algorithms.multinomial(sum(prod(m,v),b),degree,casInfo).equals(expr) ) {
					Expr result = power(sum(prod(m,v),b),degree).simplify(casInfo);
					return result;
				}else if(Algorithms.multinomial(sum(prod(m,v),neg(b)),degree,casInfo).equals(expr)) {//try the negative variant
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
			Var v = Algorithms.mostCommonVar(expr);
			
			//if its a sum of length 2 and is in polynomial form
			if(expr.isType("sum") && v!=null && expr.size() == 2 && Algorithms.isPlainPolynomial(expr,v)) {
				Func pow = null;
				Expr other = null;
				//find x^n and b, two cases x^n+b or b+x^n
				if(expr.get(0).isType("power")) {
					pow = (Func)expr.get(0);
					other = expr.get(1);
				}else if(expr.get(1).isType("power")) {
					pow = (Func)expr.get(1);
					other = expr.get(0);
				}
				
				//check if found and if b is negative and if exponent mod 2 is zero AKA even
				if(pow != null && other != null && other.negative() && Algorithms.isPositiveRealNum(pow.getExpo()) && ((Num)pow.getExpo()).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO) ) {
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
					
					if(!(newOther.isType("power") || newOther.isType("prod"))) {
						
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
	
	
	/*
	 * 
	 * factors quadratics in the general form a*x^(2*n)+b*x^n+c
	 * this includes the normal a*x^2+b*x+c
	 * 
	 * 
	 * 
	 * TODO factor(r^2+4*s^2+4*s*r) needs to work
	 */
	static Rule quadraticFactor = new Rule("factor quadratics"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			Expr expr = factor.get();
			
			
			if(expr.isType("sum")) {
				Func coefsSequence = null;
				Expr x = Algorithms.mostCommonVar(expr);
				if(x!=null) {
					coefsSequence = Algorithms.polyExtract(expr, (Var)x,casInfo);
				}
				
				if(coefsSequence != null) {
					if(coefsSequence.size() >= 3 && coefsSequence.size()%2 == 1) {//quadratic form	
						
						//verify between coefficients zero
						//for example x^4+x^2+1 in sequence form is [1,0,1,0,1]. Notice only three coefficients are non zero
						for(int i = 0;i<coefsSequence.size();i++) {
							
							if(i == 0) continue;//constant
							if(i == coefsSequence.size()-1) continue;//x^(2*n) term
							if(i == (coefsSequence.size()-1)/2) continue;//x^n term
							
							Expr currentCoef = coefsSequence.get(i);
							
							if(!currentCoef.equals(Num.ZERO)) {//all between coefficients should be zero
								return e;//invalid form
							}
						}
						
						//check discriminant
						
						//extract a b and c
						Expr a =  coefsSequence.get(coefsSequence.size()-1),b = coefsSequence.get((coefsSequence.size()-1)/2),c = coefsSequence.get(0);
						
						if(!casInfo.allowComplexNumbers() && ( (a instanceof Num && ((Num)a).isComplex()) || (b instanceof Num && ((Num)b).isComplex()) || (c instanceof Num && ((Num)c).isComplex()) )) return e;
						
						Expr discrNum = sum(power(b,num(2)),prod(num(-4),a,c)).simplify(casInfo);
						
						if( !(Algorithms.isPositiveRealNum(discrNum) || casInfo.allowComplexNumbers() || (!discrNum.negative() && casInfo.factorIrrationalRoots()) ) ) return e;
						
						boolean createsComplex = discrNum instanceof Num && (((Num)discrNum).isComplex() || ((Num)discrNum).getRealValue().signum() == -1);
						
						if(createsComplex && !casInfo.allowComplexNumbers() ) return e;
						
						Expr discrNumSqrt = sqrt(discrNum).simplify(casInfo);
						
						if(discrNumSqrt instanceof Num || casInfo.factorIrrationalRoots()) {
							
							
							Func outProd = prod();
							
							x = power(x,num((coefsSequence.size()-1)/2));
							
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
			}else if(pow.getExpo().isType("div") && Div.isNumericalAndReal((Func)pow.getExpo()) ) {
				return (Num)((Func)pow.getExpo()).getNumer();
			}else {
				return num(1);
			}
		}
		
		private Num getDenomOfPower(Func pow) {
			if(pow.getExpo().isType("div") && Div.isNumericalAndReal((Func)pow.getExpo()) ) {
				return (Num)((Func)pow.getExpo()).getDenom();
			}
			return num(1);
		}

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func factor = (Func)e;
			if(e.contains(Var.INF)) return factor;//can't factor infinity lmao
			Expr expr = factor.get();
		
			if(expr.isType("sum")) {
				boolean sumHasDiv = false;
				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i).isType("div")) {
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
						Func partsSequence = Algorithms.seperateCoef(termPower.getExpo());
						termPower = power(power(termPower.getBase(),partsSequence.get(1)),partsSequence.get(0));
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
								Func partsSequence = Algorithms.seperateCoef(otherTermPower.getExpo());
								otherTermPower = power(power(otherTermPower.getBase(),partsSequence.get(1)),partsSequence.get(0));
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
			
			if(expr.isType("prod")) {

				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i).isType("sum")) {
						Expr subSum = expr.get(i);
						subSum.setSimpleSingleNode(false);
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
			
			Var v = Algorithms.mostCommonVar(expr);
			
			if(expr.isType("sum") && v != null && Algorithms.isPlainPolynomial(expr,v) ) {
				int degree = Algorithms.degree(expr,v).intValue();
				if(degree < 2) return e;
				Func polySequence = Algorithms.polyExtract(expr,v,casInfo);
				if(polySequence == null) return e;
				for(int i = 0;i<polySequence.size();i++) if(!(polySequence.get(i) instanceof Num)) return e;//must be all nums
				ArrayList<Double> rootsAsFloat = Solve.polySolve(polySequence);
				Func outProd = prod();
				//System.out.println(expr);
				//System.out.println(rootsAsFloat);
				for(double root:rootsAsFloat) {
					if(Double.isNaN(root)) return e;
					Func rootAsPolySequence = sequence();//the polynomial to be divided
					long[] frac =  Algorithms.toFraction(root);
					rootAsPolySequence.add(num(-frac[0]));
					
					Func[] dividedSequence = null;//divided[1] is the remainder
					
					//System.out.println("r "+root+" "+degree);
					for(int i = 1;i<Math.min(8, degree);i++) {//this checks other factors like x^3-7, still integer root but a quadratic 
						//System.out.println(frac[0]+"/"+frac[1]);
						rootAsPolySequence.add(num(frac[1]));
						dividedSequence = Algorithms.polyDiv(polySequence, rootAsPolySequence, casInfo);//try polynomial division
						if(dividedSequence[1].size() == 0) break;//success
						rootAsPolySequence.set(i, num(0));//shifting to next degree
						double newApprox = Math.pow(root,i+1);
						//System.out.println(newApprox);
						frac =  Algorithms.toFraction(newApprox);//shift to  next degree
						rootAsPolySequence.set(0, num(-frac[0]));//shift to  next degree
					}
					
					
					if(dividedSequence != null && dividedSequence[1].size() == 0) {
						outProd.add( Algorithms.exprListToPoly(rootAsPolySequence, v, casInfo) );
						polySequence = dividedSequence[0];
						degree = polySequence.size()-1;
					}
					
				}
				outProd.add( Algorithms.exprListToPoly(polySequence, v, casInfo) );
				if(outProd.size() > 1) {
					return outProd.simplify(casInfo);
				}
				
			}
			return e;
			
		}
	};
}
