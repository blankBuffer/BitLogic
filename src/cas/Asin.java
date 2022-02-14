package cas;

public class Asin extends Expr{
	
	private static final long serialVersionUID = 8245957240404627757L;
	
	static Rule asinSinCase = new Rule("asin(sin(x))=x","arcsin of the sin",Rule.VERY_EASY);
	static Rule asinCosCase = new Rule("asin(cos(x))=-x+pi/2","arcsin of cosine",Rule.VERY_EASY);

	static Rule inverseUnitCircle = new Rule("asin unit circle",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("asin(0)=0","arcsin of zero",Rule.VERY_EASY),
				new Rule("asin(1)=pi/2","arcsin of one",Rule.VERY_EASY),
				new Rule("asin(sqrt(2)/2)=pi/4","arcsin of root 2 over 2",Rule.VERY_EASY),
				new Rule("asin(1/2)=pi/6","arcsin of a half",Rule.VERY_EASY),
				new Rule("asin(sqrt(3)/2)=pi/3","arcsin of root 3 over 2",Rule.VERY_EASY),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
		
	};
	
	static Rule arcsinWithSqrt = new Rule("arcsin with square root",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("asin(sqrt(a*x+b)/c)=asin((c^2-2*a*x-2*b)/c^2)/-2+pi/4","arcsin with square root",Rule.UNCOMMON),
				new Rule("asin(sqrt(x+b)/c)=asin((c^2-2*x-2*b)/c^2)/-2+pi/4","arcsin with square root",Rule.UNCOMMON),
				new Rule("asin(sqrt(a*x+b))=asin(1-2*a*x-2*b)/-2+pi/4","arcsin with square root",Rule.UNCOMMON),
				new Rule("asin(sqrt(x+b))=asin(1-2*x-2*b)/-2+pi/4","arcsin with square root",Rule.UNCOMMON),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	
	Asin(){}//
	public Asin(Expr expr) {
		add(expr);
	}
	
	static ExprList ruleSequence = null;
	
	static void loadRules(){
		ruleSequence = exprList(
				StandardRules.trigCompressInner,
				StandardRules.oddFunction,
				arcsinWithSqrt,
				asinSinCase,
				asinCosCase,
				inverseUnitCircle
		);
		Rule.initRules(ruleSequence);
	}
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.asin(get().convertToFloat(varDefs));
	}

}
