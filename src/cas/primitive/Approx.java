package cas.primitive;

import cas.*;

public class Approx extends Expr{
	
	private static final long serialVersionUID = 5922084948843351440L;

	public Approx(){
		simplifyChildren = false;
	}//
	public Approx(Expr expr,ExprList defs) {
		add(expr);
		add(defs);
		simplifyChildren = false;
	}
	
	static Rule getFloatExpr = new Rule("get float approximation",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Approx approx = (Approx)e;
			Expr out = floatExpr(approx.get().convertToFloat((ExprList)approx.get(1)));
			return out;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
			getFloatExpr	
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence(){
		return ruleSequence;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat((ExprList)get(1));//kinda pointless but whatever
	}
	
	@Override
	public String typeName() {
		return "approx";
	}
}
