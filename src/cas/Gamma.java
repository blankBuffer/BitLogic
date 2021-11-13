package cas;

import java.math.BigInteger;

public class Gamma extends Expr{
	
	private static final long serialVersionUID = 8145392107245407249L;

	public Gamma(Expr e) {
		add(e);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		if(toBeSimplified instanceof Gamma) toBeSimplified = integerCase((Gamma)toBeSimplified);
		if(toBeSimplified instanceof Gamma) toBeSimplified = fracCase((Gamma)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}
	
	Expr integerCase(Gamma gamma) {
		if(gamma.get() instanceof Num && !((Num)gamma.get()).isComplex() && ((Num)gamma.get()).realValue.compareTo(BigInteger.ZERO) == 1) {
			return num(factorial(((Num)gamma.get()).realValue.subtract(BigInteger.ONE)));
		}
		return gamma;
	}
	
	static Equ gammaOfOneHalf = (Equ)createExpr("gamma(1/2)=sqrt(pi)");
	Expr fracCase(Gamma gamma,Settings settings) {//gamma(5/2) -> 3*sqrt(pi)/4
		if(gamma.get() instanceof Div && ((Div)gamma.get()).isNumericalAndReal() ) {
			
			Expr test = gamma.modifyFromExample(gammaOfOneHalf, settings);
			if(!(test instanceof Gamma)) return test;
			
			Div casted = (Div)gamma.get();
			Sum sum = Div.mixedFraction(casted);
			if(sum != null && sum.get(1) instanceof Div && ((Div)sum.get(1)).getNumer().equalStruct(Num.ONE)  && ((Div)sum.get(1)).getDenom().equalStruct(Num.TWO)) {
				
				BigInteger n = ((Num)sum.get(0)).realValue;
				
				
				BigInteger numer = factorial(n.shiftLeft(1));
				BigInteger denom = BigInteger.valueOf(4).pow(n.intValue()).multiply(factorial(n));
				
				BigInteger gcd = numer.gcd(denom);
				
				numer = numer.divide(gcd);
				denom = denom.divide(gcd);
				
				return div(prod(num(numer),sqrt(pi())),num(denom)).simplify(settings);
				
			}
			
		}
		return gamma;
	}

	@Override
	public Expr copy() {
		Expr out = new Gamma(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		return "gamma("+get()+")";
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Gamma) {
			return other.get().equalStruct(get());
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+serialVersionUID;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat inner = ComplexFloat.sub(get().convertToFloat(varDefs),new ComplexFloat(1.0,0));
		
		return new ComplexFloat(factorial(inner.real),0);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Gamma) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}

}
