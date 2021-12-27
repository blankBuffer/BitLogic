package cas;

public class LambertW extends Expr {

	private static final long serialVersionUID = 1242113729865756736L;
	
	static Equ hasInverse = (Equ)createExpr("w(x*e^x)=x");
	static Equ productWithLog = (Equ)createExpr("w(x*ln(x))=ln(x)");
	static Equ isNegOne = (Equ)createExpr("w((-1)/e)=-1");
	static Equ ratioLog = (Equ)createExpr("w((-ln(x))/x)=-ln(x)");

	LambertW(){}//
	public LambertW(Expr e){
		add(e);
	}
	
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof LambertW) toBeSimplified = toBeSimplified.modifyFromExample(hasInverse, settings);
		if(toBeSimplified instanceof LambertW) toBeSimplified = toBeSimplified.modifyFromExample(isNegOne, settings);
		if(toBeSimplified instanceof LambertW) toBeSimplified = toBeSimplified.modifyFromExample(productWithLog, settings);
		if(toBeSimplified instanceof LambertW) toBeSimplified = toBeSimplified.modifyFromExample(ratioLog, settings);
		
		if(toBeSimplified instanceof LambertW) toBeSimplified = crazyProductRule((LambertW)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	static Expr crazyProductRuleFormat = createExpr("(k*ln(b)*b^(c/n))/n");
	static Expr crazyProductRuleFormat2 = createExpr("(ln(b)*b^(c/n))/n");
	public static Expr crazyProductRule(LambertW lam,Settings settings){
		
		Expr originalExpr = lam.get();
		
		boolean canCompute = false;
		
		ExprList equs = originalExpr.getEqusFromTemplate(crazyProductRuleFormat);
		
		Expr n=null,k=null,b=null,c=null;
		
		if(equs != null && !canCompute){
			n = Expr.getExprByName(equs, "n");
			k=Expr.getExprByName(equs, "k");
			b=Expr.getExprByName(equs, "b");
			c=Expr.getExprByName(equs, "c");
			canCompute = true;
		}else{
			equs = originalExpr.getEqusFromTemplate(crazyProductRuleFormat2);
		}
		
		if(equs != null && !canCompute){
			n = Expr.getExprByName(equs, "n");
			k=num(1);
			b=Expr.getExprByName(equs, "b");
			c=Expr.getExprByName(equs, "c");
			canCompute = true;
		}
		
		if(canCompute){
			
			Expr v = div(prod(n,lambertW( div(prod(ln(b),k,pow(b,div(c,n))),n)) ),ln(b));
			
			ComplexFloat approxV = v.convertToFloat(new ExprList());
			
			
			try{
				v = num(Math.round(approxV.real));
			}catch(Exception e){
				return lam;
			}
				
			Expr testExpr = div(prod(v,ln(b),pow(b,div(v,n))),n);
				
			Expr test = factor(sub(originalExpr,testExpr));
			test = test.simplify(settings);
			if(test.equalStruct(num(0))){
				return div(prod(v,ln(b)),n).simplify(settings);
			}
			
		}
		
		return lam;
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
		expr.INITIAL_GUESS = Math.log( x.value.real+1);
		return expr.convertToFloat(varDefs);
	}

}
