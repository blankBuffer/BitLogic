package cas;

public class Not extends Expr{
	
	private static final long serialVersionUID = 775872869042676796L;

	Not(){}//
	public Not(Expr e){
		add(e);
	}
	
	static Rule isTrue = new Rule("~true->false","true case",Rule.VERY_EASY);
	static Rule isFalse = new Rule("~false->true","false case",Rule.VERY_EASY);
	static Rule containsNot = new Rule("~~x->x","contains not",Rule.VERY_EASY);
	static Rule demorgan = new Rule("demorgan",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Not not = null;
			if(e instanceof Not){
				not = (Not)e;
			}else{
				return e;
			}
			Expr result = (not.get() instanceof Or ? new And() :(not.get() instanceof And ? new Or() :null));
			if(result != null){
				
				for(int i = 0;i<not.get().size();i++){
					result.add(not(not.get().get(i)));
				}
				result = result.simplify(settings);
				return result;
			}
			return not;
		}
	};
	
	static ExprList ruleSequence = null;
	public static void loadRules(){
		ruleSequence = exprList(
				isTrue,
				isFalse,
				containsNot,
				demorgan
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public String toString() {
		String out = "";
		out+="~";
		boolean paren = !(get() instanceof Var);
		if(paren) out+="(";
		out+=get();
		if(paren) out+=")";
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		boolean state = Math.abs(get().convertToFloat(varDefs).real) < 0.5;
		double res = state ? 1.0 : 0.0;
		return new ComplexFloat(res,0);
	}

}
