package cas.special;

import java.math.BigInteger;

import cas.*;
import cas.primitive.*;

public class Gamma extends Expr{
	
	private static final long serialVersionUID = 8145392107245407249L;

	public Gamma(){}//
	public Gamma(Expr e) {
		add(e);
	}
	
	static Rule integerCase = new Rule("gamma of an integer",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
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
		
		Rule halfCase = new Rule("gamma(1/2)->sqrt(pi)","gamma of a half",Rule.EASY);

		@Override
		public void init(){
			halfCase.init();
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
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
					
					return div(prod(num(numer),sqrt(pi())),num(denom)).simplify(casInfo);
					
				}
				
			}
			return halfCase.applyRuleToExpr(gamma, casInfo);
		}
	};

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat inner = ComplexFloat.sub(get().convertToFloat(varDefs),new ComplexFloat(1.0,0));
		
		return new ComplexFloat(factorial(inner.real),0);
	}
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
			integerCase,
			fracCase
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

}
