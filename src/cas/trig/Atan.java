package cas.trig;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.StandardRules;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Atan extends Expr{
	
	private static final long serialVersionUID = -8122799157835574716L;
	
	static Rule containsInverse = new Rule("atan(tan(x))->x","arctan of tan");

	public Atan(){}//
	public Atan(Expr expr) {
		add(expr);
	}
	
	static Rule inverseUnitCircle = new Rule("asin unit circle"){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("atan(0)->0","arctan of zero"),
				new Rule("atan(1)->pi/4","arctan of one"),
				new Rule("atan(sqrt(3))->pi/3","arctan of root 3"),
				new Rule("atan(sqrt(3)/3)->pi/6","arctan of root 3 over 3"),
				new Rule("atan(inf)->pi/2-epsilon","arctan of infinity"),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, casInfo);
			}
			return e;
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				StandardRules.trigCompressInner,
				StandardRules.oddFunction,
				containsInverse,
				inverseUnitCircle
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}


	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.atan(get().convertToFloat(varDefs));
	}
	
	@Override
	public String typeName() {
		return "acos";
	}
}
