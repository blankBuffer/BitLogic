package cas;

import java.math.BigInteger;

public class Gamma extends Expr{
	
	private static final long serialVersionUID = 8145392107245407249L;

	Gamma(){}//
	public Gamma(Expr e) {
		add(e);
	}
	
	static Rule integerCase = new Rule("gamma of an integer",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Gamma gamma = (Gamma)e;
			if(gamma.get() instanceof Num && !((Num)gamma.get()).isComplex() && ((Num)gamma.get()).realValue.compareTo(BigInteger.ZERO) == 1) {
				return num(factorial(((Num)gamma.get()).realValue.subtract(BigInteger.ONE)));
			}
			return gamma;
		}
		
	};
	
	static Expr integerCase(Gamma gamma) {
		if(gamma.get() instanceof Num && !((Num)gamma.get()).isComplex() && ((Num)gamma.get()).realValue.compareTo(BigInteger.ZERO) == 1) {
			return num(factorial(((Num)gamma.get()).realValue.subtract(BigInteger.ONE)));
		}
		return gamma;
	}
	static Rule fracCase = new Rule("gamma of an n/2",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Gamma gamma = (Gamma)e;
			
			if(gamma.get() instanceof Div && ((Div)gamma.get()).isNumericalAndReal() ) {
				
				Div casted = (Div)gamma.get();
				Sum sum = Div.mixedFraction(casted);
				if(sum != null && sum.get(1) instanceof Div && ((Div)sum.get(1)).getNumer().equals(Num.ONE)  && ((Div)sum.get(1)).getDenom().equals(Num.TWO)) {
					
					BigInteger n = ((Num)sum.get(0)).realValue;
					
					if(n.signum() == -1) return gamma;
					
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
	};

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat inner = ComplexFloat.sub(get().convertToFloat(varDefs),new ComplexFloat(1.0,0));
		
		return new ComplexFloat(factorial(inner.real),0);
	}
	
	static ExprList ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = exprList(
			integerCase,
			fracCase
		);
	}
	
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}

}
