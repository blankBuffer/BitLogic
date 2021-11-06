package cas;

import java.math.BigInteger;
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
		//toBeSimplified = generalFactor(toBeSimplified,settings);
		//toBeSimplified = reversePascalsTriangle(toBeSimplified,varcounts,settings);
		toBeSimplified = reExpandSubSums(toBeSimplified,settings);//yeah this does slow things down a bit
		toBeSimplified = pullOutNegatives(toBeSimplified,settings);
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
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
	/*
	Expr reversePascalsTriangle(Expr expr,ArrayList<VarCount> varcounts,Settings settings) {
		if(expr instanceof Sum) {
			if(expr.size() > 2 && expr.containsVars() && isPolynomial(expr,varcounts.get(0).v)) {
				
				if(varcounts.size() == 2) {
					int expo = expr.size()-1;
					
					Expr test = pow(sum(varcounts.get(0).v,varcounts.get(1).v),num(expo));
					if(Distr.powExpand(test, settings).equalStruct(expr)) return test;
					test = pow(sum(varcounts.get(0).v,neg(varcounts.get(1).v)),num(expo));
					if(Distr.powExpand(test, settings).equalStruct(expr)) return test;
					test = pow(sum(neg(varcounts.get(0).v),varcounts.get(1).v),num(expo));
					if(Distr.powExpand(test, settings).equalStruct(expr)) return test;
					
				}else if(varcounts.size() == 1) {
					System.out.println("called"+expr);
					Var v = varcounts.get(0).v;
					
					ExprList poly = polyExtract(expr,v, settings);
					if(poly == null || !(poly.get(poly.size()-1) instanceof Num) || !(poly.get(0) instanceof Num)) return expr;
					
					Num leadingCoef = (Num)poly.get(poly.size()-1);
					Num n = (Num)poly.get(0);
					
					BigInteger expo =  BigInteger.valueOf(poly.size()-1);
					BigInteger varCoef = bigRoot(leadingCoef.value.abs(), expo);
					if(!varCoef.pow(expo.intValue()).equals(leadingCoef.value.abs())) return expr;
					BigInteger constant = bigRoot(n.value.abs(), expo);
					if(!constant.pow(expo.intValue()).equals(n.value.abs())) return expr;
					
					
					for(int i = 0;i<4;i++) {//try negative and positive version
						
						Expr test = pow(sum(prod(v,num(varCoef)),num(constant)),num(expo));
						Expr toCompare = Distr.powExpand(test, settings);
						if(toCompare.equalStruct(expr)) return test.simplify(settings);
						
						constant = constant.negate();
						if(i%2==1) varCoef = varCoef.negate();
					}
					
				}
			}
			
		}
		return expr;
	}
	*/
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
	/*
	Expr generalFactor(Expr expr, Settings settings) {
		if(expr instanceof Sum) {
			//step one, combine fractions
			Sum numer = new Sum();
			Prod denom = new Prod();
			
			for(int i = 0;i < expr.size();i++) {
				//a/b+c/d= (a*d+c*b)/(b*d)
				if(expr.get(i) instanceof Power) {
					Power pow = (Power)expr.get(i);
					
					if(pow.getExpo().negative()) {
						numer = sum(prod(numer,inv(pow.copy())),inv(denom.copy()));
						denom.add(pow.copy());
						
					}else {
						numer.add(prod(pow.copy(),inv(denom.copy())));
					}
					
				}else if(expr.get(i) instanceof Prod) {
					
					Prod prod = (Prod)expr.get(i);
					Prod numerParts = new Prod();
					Prod denomParts = new Prod();
					for(int j = 0;j<prod.size();j++) {
						if(prod.get(j) instanceof Power) {
							Power pow = (Power)prod.get(j);
							
							if(pow.getExpo().negative()) {
								denomParts.add(pow.copy());
							}else {
								numerParts.add(pow.copy());
							}
							
						}else {
							numerParts.add(prod.get(j).copy());
						}
						
					}
					
					numerParts.add(inv(denom.copy()));
					numer = sum(prod(numer,inv(denomParts.copy())),numerParts);
					for(int j = 0;j<denomParts.size();j++) denom.add(denomParts.get(j).copy());
					
					
				}else {
					numer.add(prod(expr.get(i).copy(),inv(denom.copy()) ));
				}
			}
			Expr tempNumer = numer.simplify(settings);
			
			if(!(tempNumer instanceof Sum)) return prod(tempNumer,denom).simplify(settings);
			
			
			numer = (Sum)tempNumer;
			
			//end of step one
			//now we factor out gcd number
			BigInteger numFactor = numer.get(0).getCoefficient().realValue.abs();
			
			//now get the factored out number
			for(int i = 1;i<numer.size();i++) {
				numFactor = numer.get(i).getCoefficient().realValue.abs().gcd(numFactor);
			}
			
			
			
			Expr factors = new Prod();//this stores factors
			factors.add(num(numFactor));
			
			//now use the leading term to figure out what to pull out
			Prod leadingTerm = null;
			if(numer.get(0) instanceof Prod) {
				leadingTerm = (Prod)numer.get(0);
			}else {
				leadingTerm = new Prod();
				leadingTerm.add(numer.get(0));
			}
			
			for(int i = 0;i<leadingTerm.size();i++) {
				if(leadingTerm.get(i) instanceof Num) continue;
				Expr current = leadingTerm.get(i);
				Num[] frac = new Num[] {num(1),num(1)};
				if(current instanceof Power) {
					Power currentPower = (Power)current;
					Num[] fracTemp = extractNumFrac(currentPower.getExpo());
					if(fracTemp != null) {
						current = currentPower.getBase();
						frac = fracTemp;
					}
				}
				boolean allHaveIt = true;
				for(int j = 1;j < numer.size();j++) {
					Prod otherTerm = null;
					if(numer.get(j) instanceof Prod) {
						otherTerm = (Prod)numer.get(j);
					}else {
						otherTerm = new Prod();
						otherTerm.add(numer.get(j));
					}
					boolean found = false;
					for(int k = 0; k< otherTerm.size();k++) {
						if(otherTerm.get(k) instanceof Num) continue;
						Expr other = otherTerm.get(k);
						Num[] otherFrac = new Num[] {num(1),num(1)};
						if(other instanceof Power) {
							Power otherPower = (Power)other;
							Num[] otherFracTemp = extractNumFrac(otherPower.getExpo());
							if(otherFracTemp !=null) {
								other = otherPower.getBase();
								otherFrac = otherFracTemp;
							}
						}
						if(other.equalStruct(current)) {
							Num a = frac[0],b = frac[1],c = otherFrac[0],d = otherFrac[1];
							if(c.realValue.multiply(b.realValue).subtract(a.realValue.multiply(d.realValue)).multiply(BigInteger.valueOf(d.realValue.signum())).multiply(BigInteger.valueOf(b.realValue.signum())).signum() == -1) {
								frac = otherFrac;
							}
							found = true;
							break;
						}
					}
					if(!found) {
						allHaveIt = false;
						break;
					}
				}
				if(allHaveIt) {
					factors.add(pow(current,div(frac[0],frac[1])).simplify(settings));
				}
			}
			
			for(int i = 0;i<numer.size();i++) {//remove factors
				Expr innerExpr = numer.get(i);
				ArrayList<Expr> factorsNotPulledOut = new ArrayList<Expr>();
				if(!(innerExpr instanceof Prod)) {
					Expr temp = new Prod();
					temp.add(innerExpr);
					innerExpr = temp;
				}
				
				for(int j = 0;j<factors.size();j++) {
					
					Expr factor = factors.get(j);
					boolean found = false;
					for(int k = 0;k<innerExpr.size();k++) {
						Expr e = innerExpr.get(k);
						if(e.equalStruct(factor)) {
							innerExpr.remove(k);
							found = true;
							break;
						}else if(factor instanceof Num && e instanceof Num){
							Num casted = (Num)e;
							found = true;
							innerExpr.set(k, num(casted.realValue.divide(((Num)factor).realValue)));
						}
					}
					if(!found) {
						factorsNotPulledOut.add(factor);
					}
				}
				
				if(factorsNotPulledOut.size()>0) {
					for(Expr e:factorsNotPulledOut) {
						innerExpr.add(inv(e));
					}
					innerExpr = innerExpr.simplify(settings);
				}
				
				if(innerExpr instanceof Prod) {
					if(innerExpr.size() > 1) numer.set(i, innerExpr);
					else if(innerExpr.size() == 1) numer.set(i, innerExpr.get());
					else {
						numer.set(i, num(1));
					}
				}else {
					numer.set(i, innerExpr);
				}
			}
			
			factors.add(denom);
			factors = factors.simplify(settings);
			if(!factors.equalStruct(num(1))) {
				if(!(factors instanceof Prod)) {
					Expr prod = new Prod();
					prod.add(factors);
					factors = prod;
				}
				factors.add(numer);
				return factors.simplify(settings);
			}
			
		}
		return expr;
	}
	*/
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
		return get().generateHash()+9106906583202487923L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return factor(get().replace(equs));
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
