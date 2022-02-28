package cas.trig;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.Settings;
import cas.StandardRules;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Atan extends Expr{
	
	private static final long serialVersionUID = -8122799157835574716L;
	
	static Rule containsInverse = new Rule("atan(tan(x))->x","arctan of tan",Rule.VERY_EASY);

	public Atan(){}//
	public Atan(Expr expr) {
		add(expr);
	}
	
	static Rule inverseUnitCircle = new Rule("asin unit circle",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("atan(0)->0","arctan of zero",Rule.VERY_EASY),
				new Rule("atan(1)->pi/4","arctan of one",Rule.VERY_EASY),
				new Rule("atan(sqrt(3))->pi/3","arctan of root 3",Rule.VERY_EASY),
				new Rule("atan(sqrt(3)/3)->pi/6","arctan of root 3 over 3",Rule.VERY_EASY),
				new Rule("atan(inf)->pi/2-epsilon","arctan of infinity",Rule.VERY_EASY),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
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
}
