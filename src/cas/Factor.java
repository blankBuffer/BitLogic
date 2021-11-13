package cas;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;

public class Factor extends Expr{
	
	private static final long serialVersionUID = -5448276275686292911L;
	
	static Equ differenceOfSquares = (Equ) createExpr("a^2-b^2=(a-b)*(a+b)");
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
		
		ArrayList<VarCount> varcounts = new ArrayList<VarCount>();
		toBeSimplified.countVars(varcounts);
		Collections.sort(varcounts);
			
		toBeSimplified = toBeSimplified.modifyFromExample(differenceOfSquares,settings);
		toBeSimplified = toBeSimplified.modifyFromExample(sumOfCubes,settings);
		toBeSimplified = toBeSimplified.modifyFromExample(differenceOfCubes,settings);
		toBeSimplified = quadraticFactor(toBeSimplified,varcounts,settings);
		toBeSimplified = generalFactor(toBeSimplified,settings);
		toBeSimplified = pullOutIntegerRoots(toBeSimplified,varcounts,settings);
		toBeSimplified = reversePascalsTriangle(toBeSimplified,varcounts,settings);
		toBeSimplified = reExpandSubSums(toBeSimplified,settings);//yeah this does slow things down a bit
		toBeSimplified = pullOutNegatives(toBeSimplified,settings);
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr pullOutIntegerRoots(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		if(expr instanceof Sum && varcounts.size()==1 && isPolynomial(expr,varcounts.get(0).v) && degree(expr,varcounts.get(0).v).compareTo(BigInteger.ONE) == 1 ) {
			Var v = varcounts.get(0).v;
			ExprList poly = polyExtract(expr,v,settings);
			for(int i = 0;i<poly.size();i++) if(!(poly.get(i) instanceof Num)) return expr;//must be all nums
			ArrayList<BigDecimal> rootsAsDecimal = Solve.polySolve(poly);
			ArrayList<BigInteger> rootsAsInts = new ArrayList<BigInteger>();
			for(BigDecimal d:rootsAsDecimal) {//convert to ints
				BigInteger rounded = d.setScale(0, RoundingMode.HALF_UP).toBigInteger();
				if(!rootsAsInts.contains(rounded)) rootsAsInts.add(rounded);
			}
			Prod out = new Prod();
			for(BigInteger root:rootsAsInts) {
				ExprList rootAsPoly = new ExprList();
				rootAsPoly.add(num(root.negate()));
				rootAsPoly.add(num(1));
				
				ExprList[] divided = polyDiv(poly, rootAsPoly, settings);
				
				
				if(divided[1].size() == 0) {
					out.add( exprListToPoly(rootAsPoly, v, settings) );
					poly = divided[0];
				}
				
			}
			out.add( exprListToPoly(poly, v, settings) );
			
			if(out.size() > 1) {
				return out.simplify(settings);
			}
			
		}
		return expr;
	}
	
	Expr reversePascalsTriangle(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		if(expr instanceof Sum && varcounts.size() > 0 && isPolynomial(expr,varcounts.get(0).v )) {
			
			Var v = varcounts.get(0).v;
			ExprList coefs = polyExtract(expr,v,settings);
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
	
	Expr pullOutNegatives(Expr expr,Settings settings) {
		if(expr instanceof Sum && expr.negative()) {
			
			{//negate all sub elements
				for (int i = 0;i<expr.size();i++) {
					expr.set(i, neg(expr.get(i)));
				}
			}
			return neg(expr.simplify(settings));
			
		}
		return expr;
	}
	
	Expr reExpandSubSums(Expr expr,Settings settings) {
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
	
	
	
	Expr quadraticFactor(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		
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
					
					Num discrNum = num(b.realValue.pow(2).subtract(BigInteger.valueOf(4).multiply(a.realValue).multiply(c.realValue)));
				
					
					
					if(discrNum.realValue.signum() != -1 && discrNum.realValue.sqrt().pow(2).equals(discrNum.realValue)) {
					
						Expr discr = sqrt(discrNum);
						
						
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
	Expr generalFactor(Expr expr, Settings settings) {
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
				return factor(sum).simplify(settings);
			}
			Prod leadingTerm = Prod.cast(expr.get(0));
			BigInteger gcd = ((Num)leadingTerm.getCoefficient()).gcd();
			Expr factors = new Prod();
			//calculate gcd
			for(int i = 1;i<expr.size();i++) {
				gcd = ((Num)expr.get(i).getCoefficient()).gcd().gcd(gcd);
			}
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
				factors.add(factor(expr).simplify(settings));
				expr = Prod.unCast(factors);
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
