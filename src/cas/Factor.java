package cas;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

public class Factor extends Expr{
	
	private static final long serialVersionUID = -5448276275686292911L;
	
	static Equ sumOfCubes = (Equ) createExpr("a^3+b^3=(a+b)*(a^2-a*b+b^2)");
	static Equ differenceOfCubes = (Equ) createExpr("a^3-b^3=(a-b)*(a^2+a*b+b^2)");

	public Factor(Expr expr) {
		add(expr);
	}

	@Override
	public Expr simplify(Settings settings) {
		
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		toBeSimplified.simplifyChildren(settings);
		toBeSimplified = toBeSimplified.get();
		
		boolean hasVars = toBeSimplified.containsVars();
		
		ArrayList<VarCount> varcounts = null;
		if(hasVars) {
			varcounts = new ArrayList<VarCount>();
			toBeSimplified.countVars(varcounts);
			Collections.sort(varcounts);
		}
		
		toBeSimplified = toBeSimplified.modifyFromExample(sumOfCubes,settings);
		toBeSimplified = toBeSimplified.modifyFromExample(differenceOfCubes,settings);
		
		if(hasVars) toBeSimplified = quadraticFactor(toBeSimplified,varcounts,settings);//keeping this for extremely big quadratics that pull out roots cant find
		toBeSimplified = generalFactor(toBeSimplified,settings);
		
		if(hasVars) {
			toBeSimplified = power2Reduction(toBeSimplified,varcounts,settings);//x^16-1 -> (x^8+1)*(x^8-1)
			toBeSimplified = pullOutRoots(toBeSimplified,varcounts,settings);
			toBeSimplified = reversePascalsTriangle(toBeSimplified,varcounts,settings);
		}
		
		toBeSimplified = reExpandSubSums(toBeSimplified,settings);//yeah this does slow things down a bit
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	static Expr power2Reduction(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		if(expr instanceof Sum && varcounts.size() > 0 && expr.size() == 2 && isPolynomial(expr,varcounts.get(0).v)) {
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
				Expr newPow = sqrt(pow).simplify(settings);
				Expr newOther = sqrt(neg(other)).simplify(settings);
				
				if(!(newOther instanceof Power || newOther instanceof Prod)) {
					
					return prod(sum(newPow,newOther),sum(newPow,neg(newOther))).simplify(settings);
					
				}
				
			}
		}
		return expr;
	}
	
	static Expr pullOutRoots(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		if(expr instanceof Sum && varcounts.size()==1 && isPolynomial(expr,varcounts.get(0).v) ) {
			int degree = degree(expr,varcounts.get(0).v).intValue();
			if(degree < 2) return expr;
			Var v = varcounts.get(0).v;
			ExprList poly = polyExtract(expr,v,settings);
			if(poly == null) return expr;
			for(int i = 0;i<poly.size();i++) if(!(poly.get(i) instanceof Num)) return expr;//must be all nums
			ArrayList<Double> rootsAsFloat = Solve.polySolve(poly);
			Prod out = new Prod();
			//System.out.println(expr);
			//System.out.println(rootsAsFloat);
			for(double root:rootsAsFloat) {
				ExprList rootAsPoly = new ExprList();//the polynomial to be divided
				long[] frac =  toFraction(root);
				rootAsPoly.add(num(-frac[0]));
				
				ExprList[] divided = null;
				
				//System.out.println("r "+root+" "+degree);
				for(int i = 1;i<Math.min(8, degree);i++) {//this checks other factors like x^3-7, still integer root but a quadratic 
					//System.out.println(frac[0]+"/"+frac[1]);
					rootAsPoly.add(num(frac[1]));
					divided = polyDiv(poly, rootAsPoly, settings);//try polynomial division
					if(divided[1].size() == 0) break;//success
					rootAsPoly.set(i, num(0));//shifting to next degree
					double newApprox = Math.pow(root,i+1);
					//System.out.println(newApprox);
					frac =  toFraction(newApprox);//shift to  next degree
					rootAsPoly.set(0, num(-frac[0]));//shift to  next degree
				}
				
				
				if(divided != null && divided[1].size() == 0) {
					out.add( exprListToPoly(rootAsPoly, v, settings) );
					poly = divided[0];
					degree = poly.size()-1;
				}
				
			}
			out.add( exprListToPoly(poly, v, settings) );
			if(out.size() > 1) {
				return out.simplify(settings);
			}
			
		}
		return expr;
	}
	
	static Expr reversePascalsTriangle(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		if(expr instanceof Sum && varcounts.size() > 0 && isPolynomial(expr,varcounts.get(0).v )) {
			
			Var v = varcounts.get(0).v;
			ExprList coefs = polyExtract(expr,v,settings);
			if(coefs == null) return expr;
			BigInteger degree = BigInteger.valueOf(coefs.size()-1);
			if(degree.compareTo(BigInteger.TWO) == -1) return expr;
			
			Expr highestDegreeCoef = coefs.get(coefs.size()-1);
			Expr lowestDegreeCoef = coefs.get(0);
			
			Expr m = pow(highestDegreeCoef ,inv(num(degree))).simplify(settings);
			Expr b = pow(lowestDegreeCoef ,inv(num(degree))).simplify(settings);
			
			if(binomial(prod(m,v),b,degree,settings).equalStruct(expr) ) {
				return pow(sum(prod(m,v),b).simplify(settings),num(degree));
			}else if(binomial(prod(m,v),neg(b),degree,settings).equalStruct(expr)) {//try the negative variant
				return pow(sub(prod(m,v),b).simplify(settings),num(degree));
			}
			
		}
		return expr;
	}
	
	static Expr reExpandSubSums(Expr expr,Settings settings) {
		if(expr instanceof Prod) {

			for(int i = 0;i<expr.size();i++) {
				if(expr.get(i) instanceof Sum) {
					Expr subSum = expr.get(i);
					subSum.flags.simple = false;
					expr.set(i, subSum.simplify(settings));
				}
			}
		}
		
		return expr;
	}
	
	
	
	static Expr quadraticFactor(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		
		if(expr instanceof Sum) {
			ExprList coef = null;
			Var x = null;
			if(varcounts.size()>0) {
				x = varcounts.get(0).v;
				coef = polyExtract(expr, x,settings);
			}
			
			if(coef != null) {
				if(coef.size() == 3) {//quadratic
					//check discriminant
					for(int i = 0;i<coef.size();i++) {
						if(!(coef.get(i) instanceof Num)) {
							return expr;
						}
					}
					Num a =  (Num)coef.get(2),b = (Num)coef.get(1),c = (Num)coef.get(0);
					
					if(a.isComplex() || b.isComplex() || c.isComplex()) return expr;
					
					BigInteger discrNum = b.realValue.pow(2).subtract(BigInteger.valueOf(4).multiply(a.realValue).multiply(c.realValue));
				
					if(discrNum.signum() == -1) return expr;
					
					BigInteger discrNumSqrt = discrNum.sqrt();
					
					if(discrNum.signum() != -1 && discrNumSqrt.pow(2).equals(discrNum)) {
					
						Expr discr = num(discrNumSqrt);
						
						
						Expr out = new Prod();
						
						Prod twoAX = prod(num(2),a,x);
						out.add( sum(twoAX,b.copy(),prod(num(-1),discr)) );
						out.add( sum(twoAX.copy(),b.copy(),discr.copy()) );
						out.add(inv(num(4)));
						out.add(inv(a.copy()));
						
						out = out.simplify(settings);
						return out;
					}
						
					
				}
			}
		}
		
		return expr;
	}
	
	private static Num getNumerOfPower(Power pow) {
		if(pow.getExpo() instanceof Num) {
			return (Num)pow.getExpo();
		}else if(pow.getExpo() instanceof Div && ((Div)pow.getExpo()).isNumericalAndReal() ) {
			return (Num)((Div)pow.getExpo()).getNumer();
		}else {
			return num(1);
		}
	}
	private static Num getDenomOfPower(Power pow) {
		if(pow.getExpo() instanceof Div && ((Div)pow.getExpo()).isNumericalAndReal() ) {
			return (Num)((Div)pow.getExpo()).getDenom();
		}
		return num(1);
	}
	static Expr generalFactor(Expr expr, Settings settings) {
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
				return sum.simplify(settings);
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
					termPower = pow(termPower,num(1));
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
							otherTermPower = pow(otherTermPower,num(1));
						}
						
						if(otherTermPower.getBase().equalStruct(termPower.getBase())) {
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
				if(!minExpoNum.equalStruct(Num.ZERO)) {
					factors.add( Power.unCast( pow(termPower.getBase(), Div.unCast(div(minExpoNum,minExpoDen)))));
				}
			}
			
			//
			if(factors.size()>0) {
				//divide terms by factors
				for(int i = 0;i<expr.size();i++) {
					expr.set(i, div(expr.get(i),factors).simplify(settings));
				}
				//
				expr = Prod.combine(factors, factor(expr).simplify(settings));
			}
		}
		
		return expr;
	}
	
	@Override
	public Expr copy() {
		Factor out = new Factor(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="factor(";
		out+=get();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Factor) return get().equalStruct(other.get());
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+serialVersionUID;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Factor) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}

}
