package cas.special;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.Settings;
import cas.primitive.ExprList;
import cas.primitive.FloatExpr;
import cas.primitive.Sequence;
import cas.primitive.Solve;

public class LambertW extends Expr {

	private static final long serialVersionUID = 1242113729865756736L;
	
	static Rule hasInverse = new Rule("lambertW(x*e^x)->x","lambert w of product exponential",Rule.UNCOMMON);
	static Rule productWithLog = new Rule("lambertW(x*ln(x))->ln(x)","lambert w of product of logs",Rule.UNCOMMON);
	static Rule isNegOne = new Rule("lambertW((-1)/e)->-1","lambert w of -1/e",Rule.UNCOMMON);
	static Rule ratioLog = new Rule("lambertW((-ln(x))/x)->-ln(x)","lambert w of -ln(x) over x",Rule.UNCOMMON);
	
	static Rule crazyProductRule = new Rule("crazy product rule",Rule.VERY_DIFFICULT){
		private static final long serialVersionUID = 1L;
		
		Expr crazyProductRuleFormat;
		Expr crazyProductRuleFormat2;
		
		@Override
		public void init(){
			crazyProductRuleFormat = createExpr("(k*ln(b)*b^(c/n))/n");
			crazyProductRuleFormat2 = createExpr("(ln(b)*b^(c/n))/n");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			LambertW lam = (LambertW)e;
			
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
				
				Expr v = div(prod(n,lambertW( div(prod(ln(b),k,pow(b,div(c,n))),n)) ),ln(b));
				
				ComplexFloat approxV = v.convertToFloat(new ExprList());
				
				
				try{
					v = num(Math.round(approxV.real));
				}catch(Exception e2){
					return lam;
				}
					
				Expr testExpr = div(prod(v,ln(b),pow(b,div(v,n))),n);
					
				Expr test = factor(sub(originalExpr,testExpr));
				test = test.simplify(settings);
				if(test.equals(num(0))){
					return div(prod(v,ln(b)),n).simplify(settings);
				}
				
			}
			
			return lam;
		}
		
	};

	public LambertW(){}//
	public LambertW(Expr e){
		add(e);
	}
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				hasInverse,
				isNegOne,
				productWithLog,
				ratioLog,
				crazyProductRule
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public String toString() {
		String out = "lambertW(";
		out+=get();
		out+=")";
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		FloatExpr x = floatExpr(get().convertToFloat(varDefs));
		if(x.value.real<-1.0/Math.E) return ComplexFloat.ZERO;
		Solve expr = solve(equ(x,prod(var("y"),exp(var("y")))),var("y"));
		expr.INITIAL_GUESS = Math.log( x.value.real+1)*3.0/4.0;
		return expr.convertToFloat(varDefs);
	}

}
