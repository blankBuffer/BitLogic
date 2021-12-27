package cas;

import java.math.BigInteger;

public class Sin extends Expr{
	
	private static final long serialVersionUID = -5759564792496416862L;
	
	static Equ sinOfArctan = (Equ)createExpr("sin(atan(x))=x/sqrt(1+x^2)");
	static Equ sinOfAsin = (Equ)createExpr("sin(asin(x))=x");
	static Equ sinOfAcos = (Equ)createExpr("sin(acos(x))=sqrt(1-x^2)");

	Sin(){}//
	public Sin(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.modifyFromExample(sinOfArctan, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(sinOfAsin, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(sinOfAcos, settings);
		
		if(toBeSimplified instanceof Sin) {
			toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
			if(toBeSimplified.get().negative()) {
				toBeSimplified = neg(sin(neg(toBeSimplified.get())).simplify(settings) );
			}
		}
		if(toBeSimplified instanceof Sin) toBeSimplified.set(0,distr(toBeSimplified.get()).simplify(settings));
		
		if(toBeSimplified instanceof Sin) toBeSimplified = unitCircle((Sin)toBeSimplified);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	public static Expr unitCircle(Sin sin) {
		Pi pi = new Pi();
		BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4);
		
		Expr innerExpr = sin.get();
		if(innerExpr.equalStruct(num(0))) {
			return num(0);
		}else if(innerExpr instanceof Pi)
			return num(0);
		if(innerExpr instanceof Div && innerExpr.contains(pi())){
			Div frac = ((Div)innerExpr).ratioOfUnitCircle();
			
			if(frac != null) {
				BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;//getting numerator and denominator
				
				numer = numer.mod(denom.multiply(BigInteger.TWO));//restrict to the main circle
				int negate = 1;
				
				if(numer.compareTo(denom) == 1) {//if the numerator is greater than the denominator
					negate = -negate;
					numer = numer.mod(denom);//if we go past the top part of the circle we can flip it back to the top and keep track of the negative
				}
				
				if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {//if we are past the first part of the quarter circle, we can restrict it further
					numer = denom.subtract(numer);//basically reflecting across the y axis
				}
				
				if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) return num(negate);
				else if(numer.equals(BigInteger.ONE) && denom.equals(three)) return div(sqrt(num(3)),num(2*negate));
				else if(numer.equals(BigInteger.ONE) && denom.equals(six)) return inv(num(2*negate));
				else if(numer.equals(BigInteger.ONE) && denom.equals(four)) return div(sqrt(num(2)),num(2*negate));
				else if(numer.equals(BigInteger.ZERO)) return num(0);
				else {
					if(negate == -1) {
						return neg(sin(div(prod(pi(),num(numer)),num(denom)).simplify(Settings.normal)));
					}
					return sin(div(prod(pi(),num(numer)),num(denom)).simplify(Settings.normal));
				}
				
				
			}
			
		}else if(innerExpr instanceof Sum) {//sin(x-pi/4) can be turned into sin(x+7*pi/4) because sin has symmetry
			for(int i = 0;i<innerExpr.size();i++) {
				if(innerExpr.get(i) instanceof Div && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(pi)) {
					
					Div frac = ((Div)innerExpr.get(i)).ratioOfUnitCircle();
					
					if(frac!=null) {
						BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
						
						if(denom.signum() == -1){
							denom = denom.negate();
							numer = numer.negate();
						}
						
						numer = numer.mod(denom.multiply(BigInteger.TWO));//to do this we take the mod
						
						if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) {//sin(x+pi/2) = cos(x)
							innerExpr.remove(i);
							return cos(innerExpr.simplify(Settings.normal));
						}else if(numer.equals(three) && denom.equals(BigInteger.TWO)) {
							innerExpr.remove(i);
							return neg(cos(innerExpr.simplify(Settings.normal)));
						}
						
						innerExpr.set(i,  div(prod(num(numer),pi()),num(denom)) );
						sin.set(0, innerExpr.simplify(Settings.normal));
						
					}
					
				}
			}
		}
		return sin;
	}

	@Override
	public String toString() {
		String out = "";
		out+="sin(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.sin(get().convertToFloat(varDefs));
	}

}
