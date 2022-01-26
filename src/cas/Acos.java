package cas;

public class Acos extends Expr{
	
	private static final long serialVersionUID = 3855238699397076495L;

	Acos(){}
	public Acos(Expr expr) {
		add(expr);
	}
	
	static Rule containsInverse = new Rule("acos(cos(x))=x","acos contains inverse",Rule.EASY);
	static Rule containsSin = new Rule("acos(sin(x))=-x+pi/2","acos contains inverse",Rule.UNCOMMON);
	
	static Rule negativeInner = new Rule("arccos of negative value",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Acos acos = (Acos)e;
			if(acos.get().negative()){
				Expr result = sum(neg(acos(acos.get().abs(settings))),pi()).simplify(settings);
				return result;
			}
			return acos;
		}
	};
	
	static Rule inverseUnitCircle = new Rule("unit circle for arccos",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("acos(0)=pi/2","arccos of zero",Rule.VERY_EASY),
				new Rule("acos(1)=0","arccos of one",Rule.VERY_EASY),
				new Rule("acos(sqrt(2)/2)=pi/4","arccos of root 2 over 2",Rule.VERY_EASY),
				new Rule("acos(1/2)=pi/3","arccos of a half",Rule.VERY_EASY),
				new Rule("acos(sqrt(3)/2)=pi/6","arccos of root 3 over 2",Rule.VERY_EASY),
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
	
	static ExprList ruleSequence = null;
	
	static void loadRules() {
		ruleSequence = exprList(
				StandardRules.trigCompressInner,
				negativeInner,
				containsInverse,
				containsSin,
				inverseUnitCircle
		);
	}
	
	@Override
	ExprList getRuleSequence(){
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.acos(get().convertToFloat(varDefs));
	}
}
