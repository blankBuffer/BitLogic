package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.StandardRules;

public class Distr extends Expr{

	
	private static final long serialVersionUID = -1352926948237577310L;

	public Distr(){
		simplifyChildren = false;
	}//
	public Distr(Expr expr) {
		add(expr);
		simplifyChildren = false;
	}
	
	static Rule generalDistr = new Rule("general distribution",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {//2*(x+y) -> 2*x+2*y
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
					return theSum.simplify(casInfo);
				}
			}else if(expr instanceof Div) {//(x+y)/3 -> x/3+y/3
				Div casted = (Div)expr;
				casted.setNumer(distr(casted.getNumer()).simplify(casInfo));
				if(casted.getNumer() instanceof Sum) {
					for (int i = 0;i < casted.getNumer().size();i++) {
						casted.getNumer().set(i, div(casted.getNumer().get(i),casted.getDenom().copy()));
					}
					return casted.getNumer().simplify(casInfo);
					
				}
				
			}else if(expr instanceof Sum) {
				
				for(int i = 0;i<expr.size();i++) {
					expr.set(i, distr(expr.get(i)));
				}
			}
			return expr.simplify(casInfo);
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		
		ruleSequence = sequence(
				generalDistr,
				StandardRules.becomeInner
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

}
