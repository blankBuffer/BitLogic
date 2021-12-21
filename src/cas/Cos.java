package cas;

import java.math.BigInteger;

public class Cos extends Expr{
	
	private static final long serialVersionUID = -529344373251624547L;
	
	static Equ cosOfArctan = (Equ)createExpr("cos(atan(x))=1/sqrt(1+x^2)");
	static Equ cosOfArcsin = (Equ)createExpr("cos(asin(x))=sqrt(1-x^2)");
	static Equ cosOfArccos = (Equ)createExpr("cos(acos(x))=x");

	public Cos(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.modifyFromExample(cosOfArctan, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(cosOfArcsin, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(cosOfArccos, settings);
		
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0, toBeSimplified.get().abs(settings));
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0,distr(toBeSimplified.get()).simplify(settings));
		
		if(toBeSimplified instanceof Cos) toBeSimplified = unitCircle((Cos)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	public static Expr unitCircle(Cos cos,Settings settings) {
		Pi pi = new Pi();
		BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4);
		Expr innerExpr = cos.get();
		if(innerExpr.equalStruct(num(0))) {
			return num(1);
		}else if(innerExpr instanceof Pi)
			return num(-1);
		if(innerExpr instanceof Div && innerExpr.contains(pi())){
			Div frac =((Div)innerExpr).ratioOfUnitCircle();
			
			if(frac!=null) {
				
				BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
				
				numer = numer.mod(denom.multiply(BigInteger.TWO));//restrict to whole circle
				int negate = 1;
				
				if(numer.compareTo(denom) == 1) {//if we are past the top half flip over x axis
					numer = BigInteger.TWO.multiply(denom).subtract(numer);
				}
				
				if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {//if we are past quarter circle, reflect over y axis and flip sign
					numer = denom.subtract(numer);
					negate = -negate;
				}
				
				if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) return num(0);
				else if(numer.equals(BigInteger.ONE) && denom.equals(three)) return inv(num(2*negate));
				else if(numer.equals(BigInteger.ONE) && denom.equals(six)) return div(sqrt(num(3)),num(2*negate));
				else if(numer.equals(BigInteger.ONE) && denom.equals(four)) return div(sqrt(num(2)),num(2*negate));
				else if(numer.equals(BigInteger.ZERO)) return num(negate);
				else {
					//make it into the sin version for canonical form
					return prod(  num(negate),   sin(sum( div(prod(pi(),num(numer)),num(denom)) ,div(pi(),num(2))))   ).simplify(settings);
					//
					
				}
				
				
			}
			
		}else if(innerExpr instanceof Sum) {//cos(x-pi/4) can be turned into sin(x+7*pi/4) because sin has symmetry
			for(int i = 0;i<innerExpr.size();i++) {
				if(innerExpr.get(i) instanceof Div && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(pi)) {
					Div frac = ((Div)innerExpr.get(i)).ratioOfUnitCircle();
					
					if(frac!=null) {
						//make it into the sin version for canonical form
						return sin(sum( innerExpr ,div(pi(),num(2)))).simplify(settings);
						//
					}
					
				}
			}
		}
		return cos;
	}

	@Override
	public Expr copy() {
		Cos out = new Cos(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="cos(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Cos) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+8236910273651944021L;
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Cos) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.cos(get().convertToFloat(varDefs));
	}
}
