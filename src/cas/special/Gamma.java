package cas.special;

import java.math.BigInteger;

import cas.*;
import cas.primitive.*;

public class Gamma{
	
	public static Func.FuncLoader gammaLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			Rule fracCase = new Rule("gamma of an n/2"){
				Rule halfCase = new Rule("gamma(1/2)->sqrt(pi)","gamma of a half");

				@Override
				public void init(){
					halfCase.init();
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func gamma = (Func)e;
					
					if(gamma.get().typeName().equals("div") && Div.isNumericalAndReal(((Func)gamma.get())) ) {
						
						Func casted = (Func)gamma.get();
						Sum sum = Div.mixedFraction(casted);
						if(sum != null && sum.get(1).typeName().equals("div") && ((Func)sum.get(1)).getNumer().equals(Num.ONE)  && ((Func)sum.get(1)).getDenom().equals(Num.TWO)) {
							
							BigInteger n = ((Num)sum.get(0)).getRealValue();
							
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
			
			Rule integerCase = new Rule("gamma of an integer"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func gamma = (Func)e;
					if(gamma.get() instanceof Num && !((Num)gamma.get()).isComplex() && ((Num)gamma.get()).getRealValue().compareTo(BigInteger.ZERO) == 1) {
						return num(factorial(((Num)gamma.get()).getRealValue().subtract(BigInteger.ONE)));
					}
					return gamma;
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				integerCase,
				fracCase
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
					ComplexFloat inner = ComplexFloat.sub(owner.get().convertToFloat(varDefs),new ComplexFloat(1.0,0));
					
					return new ComplexFloat(Cas.factorial(inner.real),0);
				}
			};
		}
	};

}
