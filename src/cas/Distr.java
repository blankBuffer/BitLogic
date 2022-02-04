package cas;

public class Distr extends Expr{

	
	private static final long serialVersionUID = -1352926948237577310L;

	Distr(){
		simplifyChildren = false;
	}//
	public Distr(Expr expr) {
		add(expr);
		simplifyChildren = false;
	}
	
	static Rule generalDistr = new Rule("general distribution",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {//2*(x+y) -> 2*x+2*y
			Distr distr = (Distr)e;
			
			Expr expr = distr.get().copy();
			
			if(expr instanceof Prod) {
				Expr theSum = null;
				Prod prod = null;
				for(int i = 0;i<expr.size();i++) {
					if(expr.get(i) instanceof Sum) {
						theSum = expr.get(i).copy();
						prod = (Prod)expr.copy();
						prod.remove(i);
						break;
					}
				}
				if(theSum != null) {
					for(int i = 0;i<theSum.size();i++) {
						theSum.set(i, distr(Prod.combine(prod,theSum.get(i))));
					}
					return theSum.simplify(settings);
				}
			}else if(expr instanceof Div) {//(x+y)/3 -> x/3+y/3
				Div casted = (Div)expr;
				casted.setNumer(distr(casted.getNumer()).simplify(settings));
				if(casted.getNumer() instanceof Sum) {
					for (int i = 0;i < casted.getNumer().size();i++) {
						casted.getNumer().set(i, div(casted.getNumer().get(i),casted.getDenom().copy()));
					}
					return casted.getNumer().simplify(settings);
					
				}
				
			}
			return expr.simplify(settings);
		}
	};
	
	static ExprList ruleSequence = null;
	
	public static void loadRules(){
		
		ruleSequence = exprList(
				generalDistr,
				StandardRules.becomeInner
		);
		
	}
	
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

}
