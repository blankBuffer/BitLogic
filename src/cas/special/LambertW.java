package cas.special;

import cas.*;
import cas.primitive.ExprList;
import cas.primitive.Func;

/*
 * the Lambert W function which is the inverse of x*e^x
 */
public class LambertW{
	
	public static Func.FuncLoader lambertwLoader = new Func.FuncLoader(){

		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule(new Rule[]{
					hasInverse,
					isNegOneOverE,
					isZero,
					productWithLog,
					ratioLog,
					crazyProductRule
			},"main sequence");
			owner.behavior.rule.init();
			owner.behavior.toFloat = new Func.FloatFunc() {
				/*
				 * the Lambert w function is 'visually' really close to ln(x+1)*0.75 so using that as the initial guess for Newton approximation
				*/
				@Override
				public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
					//y=x*e^x  y is known x is unknown
					//f(x)=y-x*e^x finding the root of f(x) gives us lambertw(x)
					//f'(x) = -(x*e^x+e^x)
					//using newton's method the root of f(x)
					//x_n+1 = x_n-f(x_n)/f'(x_n)
					
					//x_0 = ln(x+1)*3/4 (function close to lambertw for small x values
					
					ComplexFloat y = owner.get().convertToFloat(varDefs);
					ComplexFloat x0 = ComplexFloat.mult(new ComplexFloat(3.0/4.0 , 0.0), ComplexFloat.ln( ComplexFloat.add(y, ComplexFloat.ONE) ));
					
					ComplexFloat x = x0;
				
					for(int i = 0;i<16;i++){
						ComplexFloat expX = ComplexFloat.exp(x);
						ComplexFloat xTimesExpX = ComplexFloat.mult(x, expX);
						ComplexFloat fX = ComplexFloat.sub(y, xTimesExpX);
						ComplexFloat fPrimeX = ComplexFloat.neg(ComplexFloat.add(xTimesExpX, expX));
						
						x = ComplexFloat.sub(x, ComplexFloat.div( fX , fPrimeX ));
					}
					
					return x;
				}
			};
		}
	};

	static Rule hasInverse = new Rule("lambertW(x*e^x)->x","lambert w of product exponential");
	static Rule productWithLog = new Rule("lambertW(x*ln(x))->ln(x)","lambert w of product of logs");
	static Rule isNegOneOverE = new Rule("lambertW((-1)/e)->-1","lambert w of -1/e");
	static Rule isZero = new Rule("lambertW(0)->0","lambert w pf 0");
	static Rule ratioLog = new Rule("lambertW((-ln(x))/x)->-ln(x)","lambert w of -ln(x) over x");
	
	static Rule crazyProductRule = new Rule("crazy product rule"){
		Expr crazyProductRuleFormat;
		Expr crazyProductRuleFormat2;
		
		@Override
		public void init(){
			crazyProductRuleFormat = createExpr("(k*ln(b)*b^(c/n))/n");
			crazyProductRuleFormat2 = createExpr("(ln(b)*b^(c/n))/n");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func lam = (Func)e;
			
			Expr originalExpr = lam.get();
			
			boolean canCompute = false;
			
			ExprList equs = getEqusFromTemplate(crazyProductRuleFormat,originalExpr);
			
			Expr n=null,k=null,b=null,c=null;
			
			if(equs != null && !canCompute){
				n = Rule.getExprByName(equs, "n");
				k= Rule.getExprByName(equs, "k");
				b= Rule.getExprByName(equs, "b");
				c= Rule.getExprByName(equs, "c");
				canCompute = true;
			}else{
				equs = getEqusFromTemplate(crazyProductRuleFormat2,originalExpr);
			}
			
			if(equs != null && !canCompute){
				n = Rule.getExprByName(equs, "n");
				k=num(1);
				b=Rule.getExprByName(equs, "b");
				c=Rule.getExprByName(equs, "c");
				canCompute = true;
			}
			
			if(canCompute){
				
				Expr v = div(prod(n,lambertW( div(prod(ln(b),k,power(b,div(c,n))),n)) ),ln(b));
				
				ComplexFloat approxV = v.convertToFloat(new ExprList());
				
				
				try{
					v = num(Math.round(approxV.real));
				}catch(Exception e2){
					return lam;
				}
					
				Expr testExpr = div(prod(v,ln(b),power(b,div(v,n))),n);
					
				Expr test = factor(sub(originalExpr,testExpr));
				test = test.simplify(casInfo);
				if(test.equals(num(0))){
					return div(prod(v,ln(b)),n).simplify(casInfo);
				}
				
			}
			
			return lam;
		}
		
	};
}
